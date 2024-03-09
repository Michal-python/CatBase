package cat.michal.catbase.server.packets;

import cat.michal.catbase.common.message.Message;

import java.util.concurrent.atomic.AtomicInteger;

public class PacketRedirectThread extends Thread {

    private static final AtomicInteger idAtomic = new AtomicInteger(0);

    public static final int MAX_SENDS_PER_WAKE = 100;

    private final PacketQueue packetQueue;
    private volatile boolean running = true;

    public PacketRedirectThread(PacketQueue packetQueue) {
        this.packetQueue = packetQueue;
        this.setDaemon(true);
        this.setName("Packet-Redirect-Thread-" + idAtomic.getAndIncrement());
    }

    @Override
    public void run() {
        while(running) {

            int i = 0;
            while(packetQueue.queue.size() > 0 && i < MAX_SENDS_PER_WAKE && running) {
                i++;
                Message message = packetQueue.queue.peek();

                this.packetQueue.redirectPacket(message);
            }

            try {
                this.wait();
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    void wake() {
        this.notify();
    }

    void shutdown() {
        this.running = false;
        this.wake();
    }
}
