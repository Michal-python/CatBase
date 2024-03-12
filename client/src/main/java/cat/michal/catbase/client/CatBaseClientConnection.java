package cat.michal.catbase.client;

import cat.michal.catbase.common.message.Message;
import cat.michal.catbase.common.model.CommunicationHeader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;

import java.io.IOException;
import java.net.Socket;

public record CatBaseClientConnection(Socket socket) {
    private static final ThreadLocal<ObjectWriter> cborMapper = new ThreadLocal<>() {
        @Override
        public ObjectWriter get() {
            return new CBORMapper().writer();
        }
    };

    public synchronized boolean sendPacket(Message message) {
        try {
            byte[] payload = cborMapper.get().writeValueAsBytes(message);
            CommunicationHeader header = new CommunicationHeader(payload.length);
            header.writeTo(socket().getOutputStream());
            socket().getOutputStream().write(payload);
            return true;
        } catch (IOException ignored) {
            //TODO add error handling
            return false;
        }
    }
}
