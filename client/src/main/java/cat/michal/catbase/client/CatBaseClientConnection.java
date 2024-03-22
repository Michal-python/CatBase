package cat.michal.catbase.client;

import cat.michal.catbase.common.message.Message;
import cat.michal.catbase.common.model.CatBaseConnection;
import org.jetbrains.annotations.NotNull;

import java.net.Socket;
import java.util.List;

public record CatBaseClientConnection(Socket socket, List<Message> awaitingForAck) {
    //TODO do some thread for doing something with unacknowledged packets

    public synchronized boolean sendPacket(@NotNull Message message) {
        if(message.getPacketId() < 6 && message.getPacketId() > 0) {
            awaitingForAck.add(message);
        }
        return CatBaseConnection.sendPacket(message, socket);
    }
}
