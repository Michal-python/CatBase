package cat.michal.catbase.client;

import cat.michal.catbase.client.message.AcknowledgementHandler;
import cat.michal.catbase.client.message.ErrorPacketHandler;
import cat.michal.catbase.client.message.MessageHandleResult;
import cat.michal.catbase.client.message.MessageHandler;
import cat.michal.catbase.common.LimitInputStream;
import cat.michal.catbase.common.exception.CatBaseException;
import cat.michal.catbase.common.message.Message;
import cat.michal.catbase.common.model.CommunicationHeader;
import cat.michal.catbase.common.packet.PacketType;
import cat.michal.catbase.common.packet.serverBound.AcknowledgementPacket;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;

public class CatBaseClientCommunicationThread implements Runnable {
    private static final ObjectReader cborMapper = new CBORMapper().reader();
    private final CatBaseClientConnection socket;
    private final List<MessageHandler> handlers;
    private final AcknowledgementHandler acknowledgementHandler;
    private final ErrorPacketHandler errorPacketHandler;
    private final List<ReceivedMessageHook> receivedResponses;

    public CatBaseClientCommunicationThread(CatBaseClientConnection socket, List<MessageHandler> handlers, List<ReceivedMessageHook> receivedResponses) {
        this.socket = socket;
        this.handlers = handlers;
        this.acknowledgementHandler = new AcknowledgementHandler(socket);
        this.errorPacketHandler = new ErrorPacketHandler();
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
        InputStream inputStream;
        try {
            inputStream = new BufferedInputStream(socket.socket().getInputStream());
        } catch (IOException e) {
            throw new CatBaseException(e);
        }
        while (true) {
            if(!readIncomingMessages(inputStream)) {
                return;
            }
        }
    }

    private boolean readIncomingMessages(InputStream inputStream) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(inputStream.readNBytes(12));

            CommunicationHeader communicationHeader = CommunicationHeader.create(buffer);

            if(communicationHeader == null) {
                endConnection();
                return false;
            }

            Message message = cborMapper.readValue(
                    new LimitInputStream(inputStream, communicationHeader.getLength()),
                    Message.class
            );

            if(acknowledgementHandler.handle(message).isShouldRespond()) {
                return true;
            }

            if(errorPacketHandler.handle(message).isShouldRespond()) {
                return true;
            }

            if(message.isResponse()) {
                this.receivedResponses.stream()
                        .filter(response -> response.correlationId().equals(message.getCorrelationId()))
                        .findAny().ifPresentOrElse(present -> {
                            present.consumer().accept(message);
                        }, () -> {
                            //TODO error handling client didn't expect response but got it nevertheless lol
                        });
                return true;
            }

            handlers.stream()
                    .filter(handler -> handler.regardingPacketId() == message.getPacketId())
                    .forEach(handler -> {
                        MessageHandleResult result = handler.handle(message);
                        if(result.isResponse()) {
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
                                //TODO if result.isShouldRespond await for response but it is left for further consideration
                                socket.sendPacket(new Message(
                                        new AcknowledgementPacket(result.isShouldRespond()).serialize(),
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
            endConnection();
            return false;
        }
    }
}
