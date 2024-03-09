package cat.michal.catbase.server.packets;

import cat.michal.catbase.common.message.Message;
import cat.michal.catbase.common.model.CatBaseConnection;
import cat.michal.catbase.common.packet.SerializablePayload;
import cat.michal.catbase.common.packet.serverBound.AcknowledgementPacket;
import cat.michal.catbase.common.packet.serverBound.ErrorAcknowledgementPacket;
import cat.michal.catbase.server.event.EventDispatcher;
import cat.michal.catbase.server.event.impl.ConnectionEndEvent;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public abstract class PacketQueue {
    private final String name;
    protected Queue<Message> queue;
    protected final List<SentMessage> sentPackets = new ArrayList<>();
    protected final Queue<CatBaseConnection> connections = new LinkedList<>();
    private final PacketRedirectThread packetRedirectThread;

    private final Consumer<ConnectionEndEvent> disconnectionListener;

    public PacketQueue(int queueCapacity, String name) {
        this.name = name;
        this.packetRedirectThread = new PacketRedirectThread(this);
        this.queue = new ArrayBlockingQueue<>(queueCapacity);
        this.disconnectionListener = (e) -> unsubscribe(e.getConnection());
        EventDispatcher.getInstance().hook(ConnectionEndEvent.class, this.disconnectionListener);
    }

    /**
     * Redirects packet to packet queue listeners
     * @param packet packet to redirect
     */
    public void redirectPacket(Message packet) {
        if(this.routePacket(packet))
            queue.removeIf(packetElement -> packetElement.getCorrelationId() == packet.getCorrelationId());
    }

    public boolean routePacket(Message packet) {
        synchronized (connections) {
            var connection = connections.poll();
            if (connection == null) return false;
            connections.add(connection);
            return sendPacket(connection, packet);
        }
    }

    public void addPacket(@NotNull Message packet) {
        SerializablePayload payload = packet.deserializePayload();
        if(payload instanceof AcknowledgementPacket ackPacket) {
            sentPackets.removeIf(toSendElement -> toSendElement.message.getCorrelationId() == ackPacket.correlationId);
            return;
        } else if (payload instanceof ErrorAcknowledgementPacket errPacket) {
            var index = IntStream.range(0, sentPackets.size())
                    .filter(i -> sentPackets.get(i).message.getCorrelationId() == errPacket.correlationId)
                    .findFirst();
            if (index.isPresent()) {
                var sentMessage = sentPackets.remove(index.getAsInt());
                addPacket(sentMessage.message);
            }
            return;
        }
        queue.add(packet);
        packetRedirectThread.wake();
    }

    public void subscribe(CatBaseConnection connection) {
        connections.add(connection);
        packetRedirectThread.wake();
    }

    public void unsubscribe(CatBaseConnection connection) {
        synchronized (connections) {
            connections.remove(connection);
        }
        // Requeue all unprocessed messages
        sentPackets.removeIf(p -> {
            if (p.connection.get() == connection) {
                queue.add(p.message);
                return true;
            } return false;
        });
    }

    private boolean sendPacket(CatBaseConnection connection, Message packet) {
        sentPackets.add(new SentMessage(packet, new WeakReference<>(connection)));
        return connection.sendPacket(packet);
    }

    public void shutdown() {
        EventDispatcher.getInstance().unhook(this.disconnectionListener);
        queue.clear();
        sentPackets.clear();
        packetRedirectThread.shutdown();
    }

    public String getName() {
        return name;
    }

    public record SentMessage(
            Message message,
            WeakReference<CatBaseConnection> connection
    ) {}
}
