package cat.michal.catbase.server.exchange;

import cat.michal.catbase.common.message.Message;
import cat.michal.catbase.server.packets.PacketQueue;

import java.util.List;

public interface Exchange {
    boolean route(Message message);

    List<PacketQueue> queues();

    String getName();

    String getTypeName();
}
