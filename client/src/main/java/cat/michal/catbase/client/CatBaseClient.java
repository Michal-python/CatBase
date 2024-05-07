package cat.michal.catbase.client;

import cat.michal.catbase.client.message.MessageHandler;
import cat.michal.catbase.common.converter.AbstractMessageConverter;
import cat.michal.catbase.common.data.ListKeeper;
import cat.michal.catbase.common.exception.CatBaseException;
import cat.michal.catbase.common.message.Message;
import cat.michal.catbase.common.model.CatBaseConnection;
import cat.michal.catbase.common.packet.PacketType;
import cat.michal.catbase.common.packet.clientBound.ErrorPacket;
import cat.michal.catbase.common.packet.serverBound.QueueSubscribePacket;
import cat.michal.catbase.common.packet.serverBound.QueueUnsubscribePacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("all")
public class CatBaseClient {
    private static final Logger                     logger = LoggerFactory.getLogger(CatBaseClient.class);
    private static final AtomicInteger              threadId = new AtomicInteger(0);
    private static final AtomicInteger              retries = new AtomicInteger(0);
    private final CatBaseClientConfig               config;
    private final List<ReceivedHook<Message>>       receivedResponses;
    private final List<ReceivedHook<ErrorPacket>>   receivedAcknowledgements;
    private volatile boolean                        shouldBeConnected;
    private CatBaseClientConnection                 socket;
    private final Queue<Message>                    outgoingQueue = new LinkedList<>();

    CatBaseClient(CatBaseClientConfig config) {
        this.config = config;

        config.setHandlers(new ArrayList<>(config.getHandlers()));
        this.receivedResponses = ListKeeper.getInstance().createDefaultTimeList();
        this.receivedAcknowledgements = ListKeeper.getInstance().createDefaultTimeList();
    }

    public void registerHandler(MessageHandler messageHandler) {
        this.config.getHandlers().add(messageHandler);
    }

    public List<ReceivedHook<ErrorPacket>> getReceivedAcknowledgements() {
        return receivedAcknowledgements;
    }

    public AbstractMessageConverter getConverter() {
        return config.getAbstractMessageConverter();
    }

    private Runnable onClientConnectionEnd() {
        return () -> {
            logger.debug("Connection with server %s:%s dropped".formatted(
                    config.getAddress().getHostAddress(), config.getPort()
            ));
            if(this.shouldBeConnected) {
                if(retries.get() > 0) {
                    try {
                        config.getRetryInterval().sleep(config.getRetryIntervalCount());
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                if(retries.incrementAndGet() > config.getMaxRetriesCount()) {
                    retries.set(0);
                    throw new CatBaseException("Failed to connect to server %s on port %s after %s retries".formatted(
                            config.getAddress().getHostAddress(), config.getPort(), config.getMaxRetriesCount()
                    ));
                }
                logger.info("Retrying to connect to server %s:%s".formatted(
                        config.getAddress().getHostAddress(), config.getPort()
                ));

                this.connectInternal();
            }
        };
    }

    private void connectInternal() {
        try {
            Socket socket = new Socket(this.config.getAddress(), this.config.getPort());

            socket.setKeepAlive(true);
            socket.setReuseAddress(true);
            this.socket = new CatBaseClientConnection(
                    new CatBaseConnection(UUID.randomUUID(), socket),
                    ListKeeper.getInstance().createDefaultTimeList()
            );

            new Thread(new CatBaseClientCommunicationThread(this.socket,
                    this.config.getHandlers(),
                    this.receivedResponses,
                    this,
                    this.onClientConnectionEnd()), "Client-Thread-" + threadId.incrementAndGet()).start();


            sendAndReceiveAck(new Message(
                    config.getCredentials().wrapCredentials().serialize(),
                    UUID.randomUUID(),
                    PacketType.HANDSHAKE.getId(),
                    null,
                    null
            )).get();
            logger.info("Connected with " + config.getAddress().getHostAddress() + ":" + config.getPort());
            retries.set(0);

            synchronized (outgoingQueue) {
                if (outgoingQueue.size() > 0) {
                    logger.info("Resending " + outgoingQueue.size() + " packets");
                    Message message;
                    while((message = outgoingQueue.poll()) != null) {
                        if(!this.socket.sendPacket(message)) {
                            outgoingQueue.add(message);
                            onClientConnectionEnd().run();
                            break;
                        }
                    }
                }
            }


        } catch (IOException | ExecutionException | InterruptedException e) {
            logger.warn("Connection failed due to exception: " + e.getMessage());
            onClientConnectionEnd().run();
        }
    }

    public void connect() throws CatBaseException {
        if(this.isConnected() || this.shouldBeConnected) {
            throw new CatBaseException("Client is already connected");
        }

        this.shouldBeConnected = true;
        connectInternal();
    }

    public void disconnect() throws CatBaseException {
        try {
            if(this.socket != null) {
                this.socket.socket().close();
            }
            this.shouldBeConnected = false;
            this.socket = null;
        } catch (IOException e) {
            throw new CatBaseException(e);
        }
    }

    public boolean isConnected() {
        if(socket == null) {
            return false;
        }
        return this.socket.socket().isOpen();
    }

    public boolean subscribe(String queueName) {
        try {
            sendAndReceiveAck(new Message(
                    new QueueSubscribePacket(queueName).serialize(),
                    UUID.randomUUID(),
                    PacketType.SUBSCRIBE.getId(),
                    null,
                    null
            )).get();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean unsubscribe(String queueName) {
        try {
            sendAndReceiveAck(new Message(
                    new QueueUnsubscribePacket(queueName).serialize(),
                    UUID.randomUUID(),
                    PacketType.UNSUBSCRIBE.getId(),
                    null,
                    null
            )).get();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    CompletableFuture<ErrorPacket> sendAndReceiveAck(Message message) {
        CompletableFuture<ErrorPacket> future = new CompletableFuture<>();
        UUID hookId = UUID.randomUUID();

        this.receivedAcknowledgements.add(new ReceivedHook<>(hookId, message.getCorrelationId(), msg -> {
            this.receivedAcknowledgements.removeIf(ackElement -> ackElement.id().equals(hookId));
            future.complete(msg);
        }));

        this.sendMessage(message);

        return future;
    }

    public void send(Message message) {
        this.sendMessage(message);
    }

    public <T> void convertAndSend(T message, String exchangeName, String routingKey) {
        try {
            Message newMessage = config.getAbstractMessageConverter().encode(message);
            newMessage.setExchangeName(exchangeName);
            newMessage.setRoutingKey(routingKey);


            this.sendMessage(newMessage);
        } catch (Exception e) {
            throw new CatBaseException(e);
        }
    }

    public <T> CompletableFuture<T> convertSendAndReceiveAsType(T message, String exchangeName, String routingKey) {
        return convertSendAndReceive(message, exchangeName, routingKey).thenApply((m) -> {
            try {
                return (T) getConverter().decode(m);
            } catch (Exception e) {
                throw new CatBaseException(e);
            }
        });
    }

    public <T> CompletableFuture<Message> convertSendAndReceive(T message, String exchangeName, String routingKey) {
        CompletableFuture<Message> future = new CompletableFuture<>();
        UUID hookId = UUID.randomUUID();
        Message newMessage = null;
        try {
            newMessage = config.getAbstractMessageConverter().encode(message);
            newMessage.setExchangeName(exchangeName);
            newMessage.setRoutingKey(routingKey);
        } catch (Exception e) {
            throw new CatBaseException(e);
        }

        this.receivedResponses.add(new ReceivedHook<>(hookId, newMessage.getCorrelationId(), msg -> {
            this.receivedResponses.removeIf(responseElement -> responseElement.id().equals(hookId));
            future.complete(msg);
        }));


        this.sendMessage(newMessage.setShouldRespond(true));

        return future;
    }

    public CompletableFuture<Message> sendAndReceive(Message message) {
        CompletableFuture<Message> future = new CompletableFuture<>();
        UUID hookId = UUID.randomUUID();

        this.receivedResponses.add(new ReceivedHook<>(hookId, message.getCorrelationId(), msg -> {
            this.receivedResponses.removeIf(responseElement -> responseElement.id().equals(hookId));
            future.complete(msg);
        }));

        sendMessage(message.setShouldRespond(true));
        return future;
    }

    private void sendMessage(Message message) {
        synchronized(outgoingQueue) {
            if (!socket.sendPacket(message)) {
                outgoingQueue.add(message);
                onClientConnectionEnd().run();
            }
        }
    }

    public <T> CompletableFuture<T> sendAndReceiveAsType(Message message, Class<T> ignored) {
        return sendAndReceive(message).thenApply((m) -> {
            try {
                return (T) getConverter().decode(m);
            } catch (Exception e) {
                throw new CatBaseException(e);
            }
        });
    }
}
