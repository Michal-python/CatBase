package cat.michal.catbase.common.model;

import cat.michal.catbase.common.message.Message;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;

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

    public synchronized void sendPacket(Message packet) {
        try {
            byte[] payload = cborMapper.get().writeValueAsBytes(packet);
            CommunicationHeader header = new CommunicationHeader(payload.length);
            header.writeTo(socket().getOutputStream());
            socket().getOutputStream().write(payload);
        } catch (IOException ignored) {
            //TODO add error handling
        }
    }
}
