package cat.michal.catbase.common.model;

import cat.michal.catbase.common.message.Message;
import cat.michal.catbase.common.packet.PacketType;
import cat.michal.catbase.common.packet.clientBound.ErrorPacket;
import cat.michal.catbase.common.packet.serverBound.AcknowledgementPacket;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.Socket;
import java.util.UUID;

public record CatBaseConnection(UUID id, Socket socket) {
    private static final ThreadLocal<ObjectWriter> cborMapper = new ThreadLocal<>() {
        @Override
        public ObjectWriter get() {
            return new CBORMapper().writer();
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
                    new AcknowledgementPacket().serialize(),
                    reference.getCorrelationId(),
                    PacketType.ACKNOWLEDGEMENT_PACKET.getId(),
                    null,
                    null
            ));
        } catch (JsonProcessingException ignored) {
        }
    }

    public synchronized boolean sendPacket(Message packet) {
        return sendPacket(packet, socket);
    }

    public static synchronized boolean sendPacket(Message packet, Socket socket) {
        try {
            byte[] payload = cborMapper.get().writeValueAsBytes(packet);
            CommunicationHeader header = new CommunicationHeader(payload.length);
            header.writeTo(socket.getOutputStream());
            socket.getOutputStream().write(payload);
            return true;
        } catch (IOException ignored) {
            //TODO add error handling
            return false;
        }
    }
}
