package cat.michal.catbase.server.defaultImpl;

import cat.michal.catbase.common.message.Message;
import cat.michal.catbase.common.model.CatBaseConnection;
import cat.michal.catbase.server.exchange.Exchange;
import cat.michal.catbase.server.packets.PacketQueue;

import java.util.List;

public class DirectExchange implements Exchange {
    private final List<PacketQueue> queues;
    private final String name;

    public DirectExchange(String name, List<PacketQueue> queues) {
        this.queues = queues;
        this.name = name;
    }

    @Override
    public boolean route(Message message, CatBaseConnection connection) {
        boolean routed = false;
        for(PacketQueue queue : queues) {
            if (queue.getName().equals(message.getRoutingKey())) {
                queue.addPacket(message, connection);
                routed = true;
            }
        }
        return routed;
    }

    @Override
    public List<PacketQueue> queues() {
        return queues;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getTypeName() {
        return "direct";
    }
}
