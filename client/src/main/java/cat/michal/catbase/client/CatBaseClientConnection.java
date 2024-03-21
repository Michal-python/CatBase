package cat.michal.catbase.client;

import cat.michal.catbase.common.message.Message;
import cat.michal.catbase.common.model.CatBaseConnection;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;

import java.net.Socket;

public record CatBaseClientConnection(Socket socket) {
    private static final ThreadLocal<ObjectWriter> cborMapper = new ThreadLocal<>() {
        @Override
        public ObjectWriter get() {
            return new CBORMapper().writer();
        }
    };

    public synchronized boolean sendPacket(Message message) {
        return CatBaseConnection.sendPacket(message, socket);
    }
}
