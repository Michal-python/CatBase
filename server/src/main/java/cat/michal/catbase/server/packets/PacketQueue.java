package cat.michal.catbase.server.packets;

import cat.michal.catbase.common.message.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Consumer;

public class PacketQueue {
    private int queueCapacity;
    private String name;
    private final Queue<Message> queue = new ArrayBlockingQueue<>(queueCapacity);
    private final List<PacketQueueListener> packetQueueListeners = new ArrayList<>();

    private PacketQueue(int queueCapacity, String name) {
        this.queueCapacity = queueCapacity;
        this.name = name;
    }
}
