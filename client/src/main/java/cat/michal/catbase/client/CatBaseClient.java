package cat.michal.catbase.client;

import cat.michal.catbase.client.message.MessageHandler;
import cat.michal.catbase.common.auth.AuthCredentials;
import cat.michal.catbase.common.exception.CatBaseException;
import cat.michal.catbase.common.message.Message;
import cat.michal.catbase.common.packet.PacketType;
import cat.michal.catbase.common.packet.serverBound.QueueSubscribePacket;
import cat.michal.catbase.common.packet.serverBound.QueueUnsubscribePacket;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class CatBaseClient implements BaseClient {
    private CatBaseClientConnection socket;
    private final int awaitingQueueSize;
    private final AuthCredentials credentials;
    private final List<MessageHandler> handlers;
    //TODO need thread for removing messages that haven't got a response
    private final List<ReceivedMessageHook> receivedResponses;


    public CatBaseClient(AuthCredentials credentials, List<MessageHandler> handlers, int awaitingQueueSize) {
        this.credentials = credentials;
        this.handlers = handlers;
        this.awaitingQueueSize = awaitingQueueSize;
        this.receivedResponses = new ArrayList<>();
    }

    public void registerHandler(MessageHandler messageHandler) {
        this.handlers.add(messageHandler);
    }

    @Override
    public void connect(String addr, int port) throws CatBaseException {
        if(socket != null) {
            throw new CatBaseException("Client is already connected");
        }

        try(Socket socket = new Socket(addr, port)) {
            socket.setKeepAlive(true);
            socket.setReuseAddress(true);
            this.socket = new CatBaseClientConnection(socket, new ArrayList<>(this.awaitingQueueSize));

            new Thread(new CatBaseClientCommunicationThread(this.socket, this.handlers, this.receivedResponses)).start();
            this.socket.sendPacket(new Message(
                    credentials.wrapCredentials().serialize(),
                    UUID.randomUUID(),
                    PacketType.HANDSHAKE.getId(),
                    null,
                    null
            ));
        } catch (IOException e) {
            throw new CatBaseException(e);
        }
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

    public boolean send(Message message) {
        return socket.sendPacket(message);
    }

    public boolean subscribe(String queueName) {
        try {
            this.socket.sendPacket(new Message(
                    new QueueSubscribePacket(queueName).serialize(),
                    UUID.randomUUID(),
                    PacketType.SUBSCRIBE.getId(),
                    null,
                    null
            ));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean unsubscribe(String queueName) {
        try {
            this.socket.sendPacket(new Message(
                    new QueueUnsubscribePacket(queueName).serialize(),
                    UUID.randomUUID(),
                    PacketType.UNSUBSCRIBE.getId(),
                    null,
                    null
            ));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public CompletableFuture<Message> sendAndReceive(Message message) {
        socket.sendPacket(message);

        CompletableFuture<Message> future = new CompletableFuture<>();
        UUID hookId = UUID.randomUUID();

        this.receivedResponses.add(new ReceivedMessageHook(hookId, message.getCorrelationId(), msg -> {
            this.receivedResponses.removeIf(responseElement -> responseElement.id().equals(hookId));
            future.complete(msg);
        }));
        return future;
    }
}
