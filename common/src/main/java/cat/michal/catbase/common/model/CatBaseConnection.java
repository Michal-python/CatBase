package cat.michal.catbase.common.model;

import cat.michal.catbase.common.message.Message;
import cat.michal.catbase.common.packet.PacketType;
import cat.michal.catbase.common.packet.clientBound.ErrorPacket;
import cat.michal.catbase.common.packet.serverBound.AcknowledgementPacket;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.Socket;
import java.util.UUID;

public class CatBaseConnection {

    private final UUID id;
    private final Socket socket;
    private final JsonParser parser;

    public CatBaseConnection(UUID id, Socket socket) throws IOException{
        this.id = id;
        this.socket = socket;
        this.parser = cborFactory.get().createParser(socket.getInputStream());
    }

    private static final ThreadLocal<ObjectMapper> cborMapper = new ThreadLocal<>() {
        @Override
        public ObjectMapper get() {
            return new CBORMapper();
        }
    };

    private static final ThreadLocal<CBORFactory> cborFactory = new ThreadLocal<>() {
        @Override
        public CBORFactory get() {
            var factory = new CBORFactory();
            factory.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
            return factory;
        }
    };

    public void sendError(@NotNull ErrorPacket error, Message reference) {
        try {
            sendPacket(new Message(
                    error.serialize(),
                    reference.getCorrelationId(),
                    PacketType.ERROR_PACKET.getId(),
                    null,
                    null
            ));
        } catch (JsonProcessingException ignored) {
        }
    }

    public void sendAcknowledgement(@NotNull Message reference) {
        try {
            sendPacket(new Message(
                    new AcknowledgementPacket(false).serialize(),
                    reference.getCorrelationId(),
                    PacketType.ACKNOWLEDGEMENT_PACKET.getId(),
                    null,
                    null
            ));
        } catch (JsonProcessingException ignored) {
        }
    }

    public void close() throws IOException {
        this.socket.close();
        this.parser.close();
    }

    public synchronized Message readMessage() throws IOException {
        return cborMapper.get().readValue(parser, Message.class);
    }

    public UUID getId() {
        return id;
    }

    public synchronized boolean sendPacket(Message packet) {
        return sendPacket(packet, socket);
    }

    public static synchronized boolean sendPacket(Message packet, Socket socket) {
        try {
            cborMapper.get().writeValue(socket.getOutputStream(), packet);
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }
}
