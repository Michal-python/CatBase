package cat.michal.catbase.server.defaultImpl;

import cat.michal.catbase.server.packets.PacketQueue;

public class DefaultQueue extends PacketQueue {
    public DefaultQueue(int queueCapacity, String name) {
        super(queueCapacity, name);
    }
}
