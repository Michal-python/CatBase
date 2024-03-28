package cat.michal.catbase.client;

import cat.michal.catbase.common.message.Message;
import cat.michal.catbase.common.model.CatBaseConnection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record CatBaseClientConnection(CatBaseConnection socket, List<Message> awaitingForAck) {

    public synchronized boolean sendPacket(@NotNull Message message) {
        if(message.getPacketId() < 6 && message.getPacketId() > 0) {
            awaitingForAck.add(message);
        }
        return socket.sendPacket(message);
    }
}
