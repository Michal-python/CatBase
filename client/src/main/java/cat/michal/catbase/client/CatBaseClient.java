package cat.michal.catbase.client;

import cat.michal.catbase.client.message.MessageHandler;
import cat.michal.catbase.common.auth.AuthCredentials;
import cat.michal.catbase.common.data.ListKeeper;
import cat.michal.catbase.common.exception.CatBaseException;
import cat.michal.catbase.common.message.Message;
import cat.michal.catbase.common.model.CatBaseConnection;
import cat.michal.catbase.common.packet.PacketType;
import cat.michal.catbase.common.packet.clientBound.ErrorPacket;
import cat.michal.catbase.common.packet.serverBound.QueueSubscribePacket;
import cat.michal.catbase.common.packet.serverBound.QueueUnsubscribePacket;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("all")
public class CatBaseClient implements BaseClient {
    private CatBaseClientConnection socket;
    private final AuthCredentials credentials;
    private final List<MessageHandler> handlers;
    private final List<ReceivedHook<Message>> receivedResponses;
    private final List<ReceivedHook<ErrorPacket>> receivedAcknowledgements;
    private static final AtomicInteger threadId = new AtomicInteger(0);

    public CatBaseClient(AuthCredentials credentials, List<MessageHandler> handlers) {
        this.credentials = credentials;
        this.handlers = new ArrayList<>(handlers);
        this.receivedResponses = ListKeeper.getInstance().createDefaultTimeList();
        this.receivedAcknowledgements = ListKeeper.getInstance().createDefaultTimeList();
    }

    public void registerHandler(MessageHandler messageHandler) {
        this.handlers.add(messageHandler);
    }

    @Override
    public void connect(String addr, int port) throws CatBaseException {
        if(socket != null) {
            throw new CatBaseException("Client is already connected");
        }

        try {
            Socket socket = new Socket(addr, port);
            socket.setKeepAlive(true);
            socket.setReuseAddress(true);
            this.socket = new CatBaseClientConnection(new CatBaseConnection(UUID.randomUUID(), socket), ListKeeper.getInstance().createDefaultTimeList());

            new Thread(new CatBaseClientCommunicationThread(this.socket, this.handlers, this.receivedResponses, this), "Client-Thread-" + threadId.incrementAndGet()).start();

            UUID authPacketId = UUID.randomUUID();

            sendAndReceiveAck(new Message(
                    credentials.wrapCredentials().serialize(),
                    authPacketId,
                    PacketType.HANDSHAKE.getId(),
                    null,
                    null
            )).get();

        } catch (IOException e) {
            throw new CatBaseException(e);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public List<ReceivedHook<ErrorPacket>> getReceivedAcknowledgements() {
        return receivedAcknowledgements;
    }

    @Override
    public void disconnect() throws CatBaseException {
        try {
            this.socket.socket().close();
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

    public void send(Message message) {
        this.socket.sendPacket(message);
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
            sendAndReceive(new Message(
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

    public CompletableFuture<ErrorPacket> sendAndReceiveAck(Message message) {
        CompletableFuture<ErrorPacket> future = new CompletableFuture<>();
        UUID hookId = UUID.randomUUID();

        this.receivedAcknowledgements.add(new ReceivedHook<>(hookId, message.getCorrelationId(), msg -> {
            this.receivedAcknowledgements.removeIf(responseElement -> responseElement.id().equals(hookId));
            future.complete(msg);
        }));

        socket.sendPacket(message);

        return future;
    }

    public CompletableFuture<Message> sendAndReceive(Message message) {
        CompletableFuture<Message> future = new CompletableFuture<>();
        UUID hookId = UUID.randomUUID();

        this.receivedResponses.add(new ReceivedHook<>(hookId, message.getCorrelationId(), msg -> {
            this.receivedResponses.removeIf(responseElement -> responseElement.id().equals(hookId));
            future.complete(msg);
        }));

        socket.sendPacket(message);
        return future;
    }
}
