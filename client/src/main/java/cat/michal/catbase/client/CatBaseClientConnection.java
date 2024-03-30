package cat.michal.catbase.client;

import cat.michal.catbase.common.message.Message;
import cat.michal.catbase.common.model.CatBaseConnection;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public record CatBaseClientConnection(CatBaseConnection socket, List<UUID> awaitingForAck) {

    public boolean sendPacket(@NotNull Message message) {
        if(message.getPacketId() < 6 && message.getPacketId() > 0) {
            awaitingForAck.add(message.getCorrelationId());
        }
        return socket.sendPacket(message);
    }
}
