package cat.michal.catbase.client;

import cat.michal.catbase.common.exception.CatBaseException;
import cat.michal.catbase.common.message.Message;
import cat.michal.catbase.common.packet.PacketType;
import cat.michal.catbase.common.packet.serverBound.HandshakePacket;

import java.io.IOException;
import java.net.Socket;
import java.util.UUID;

public class CatBaseClient implements BaseClient {
    private CatBaseClientConnection socket;

    @Override
    public void connect(String addr, int port) throws CatBaseException {
        try(Socket socket = new Socket(addr, port)) {
            socket.setKeepAlive(true);
            socket.setReuseAddress(true);
            this.socket = new CatBaseClientConnection(socket);

            new Thread(new CatBaseClientCommunicationThread(socket)).start();
            this.socket.sendPacket(new Message(
                    new HandshakePacket("login", "password").serialize(),
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
        } catch (IOException e) {
            throw new CatBaseException(e);
        }
    }
}
