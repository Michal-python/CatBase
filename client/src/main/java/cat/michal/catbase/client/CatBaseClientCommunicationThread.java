package cat.michal.catbase.client;

import cat.michal.catbase.client.message.AcknowledgementHandler;
import cat.michal.catbase.client.message.ErrorPacketHandler;
import cat.michal.catbase.client.message.MessageHandleResult;
import cat.michal.catbase.client.message.MessageHandler;
import cat.michal.catbase.common.message.Message;
import cat.michal.catbase.common.packet.PacketType;
import cat.michal.catbase.common.packet.serverBound.AcknowledgementPacket;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class CatBaseClientCommunicationThread implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(CatBaseClientCommunicationThread.class);
    private final CatBaseClientConnection socket;
    private final List<MessageHandler> handlers;
    private final AcknowledgementHandler acknowledgementHandler;
    private final ErrorPacketHandler errorPacketHandler;
    private final List<ReceivedHook<Message>> receivedResponses;

    public CatBaseClientCommunicationThread(CatBaseClientConnection socket, List<MessageHandler> handlers, List<ReceivedHook<Message>> receivedResponses, CatBaseClient client) {
        this.socket = socket;
        this.handlers = handlers;
        this.acknowledgementHandler = new AcknowledgementHandler(socket, client);
        this.errorPacketHandler = new ErrorPacketHandler(client);
        this.receivedResponses = receivedResponses;
    }

    public void endConnection() {
        try {
            this.socket.socket().close();
        } catch (IOException ignored) {
        }
        Thread.currentThread().interrupt();
    }

    @Override
    public void run() {
        while (true) {
            if(!readIncomingMessages()) {
                logger.debug("Client side end conn");
                return;
            }
        }
    }

    private boolean readIncomingMessages() {
        try {
            Message message = this.socket.socket().readMessage();

            logger.debug("Client received packet " + message);

            if(acknowledgementHandler.handle(message).isShouldRespond()) {
                return true;
            }

            if(errorPacketHandler.handle(message).isShouldRespond()) {
                return true;
            }

            if(message.isResponse()) {
                this.receivedResponses.stream()
                        .filter(response -> response.correlationId().equals(message.getCorrelationId()))
                        .findAny().ifPresentOrElse(present -> present.consumer().accept(message), () -> logger.warn("Didn't expect a response but got it nevertheless %s".formatted(message.getCorrelationId())));
                return true;
            }

            handlers.stream()
                    .filter(handler -> handler.regardingPacketId() == message.getPacketId())
                    .forEach(handler -> {
                        MessageHandleResult result = handler.handle(message);
                        if(result != null && result.isResponse()) {
                            socket.sendPacket(
                                    new Message(
                                            result.getResponse().getPayload(),
                                            message.getCorrelationId(),
                                            result.getResponse().getPacketId(),
                                            message.getRoutingKey(),
                                            message.getExchangeName()
                                    ).setResponse(true)
                            );
                        } else {
                            try {
                                socket.sendPacket(new Message(
                                        new AcknowledgementPacket(false).serialize(),
                                        message.getCorrelationId(),
                                        PacketType.ACKNOWLEDGEMENT_PACKET.getId(),
                                        message.getRoutingKey(),
                                        message.getExchangeName()
                                ));
                            } catch (JsonProcessingException ignored) {
                            }
                        }
                    });

            return true;
        } catch (Exception e) {
            logger.debug("Client recv error", e);
            endConnection();
            return false;
        }
    }
}
