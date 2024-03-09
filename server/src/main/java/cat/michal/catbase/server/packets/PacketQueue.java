package cat.michal.catbase.server.packets;

import cat.michal.catbase.common.message.Message;
import cat.michal.catbase.common.model.CatBaseConnection;
import cat.michal.catbase.common.packet.SerializablePayload;
import cat.michal.catbase.common.packet.serverBound.AcknowledgementPacket;
import cat.michal.catbase.common.packet.serverBound.ErrorAcknowledgementPacket;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.IntStream;

public abstract class PacketQueue {
    private final String name;
    protected Queue<Message> queue;
    protected final List<Message> sentPackets = new ArrayList<>();
    protected final Queue<CatBaseConnection> connections = new LinkedList<>();
    private final PacketRedirectThread packetRedirectThread;

    public PacketQueue(int queueCapacity, String name) {
        this.name = name;
        this.packetRedirectThread = new PacketRedirectThread(this);
        this.queue = new ArrayBlockingQueue<>(queueCapacity);
    }

    /**
     * Redirects packet to packet queue listeners
     * @param packet packet to redirect
     */
    public void redirectPacket(Message packet) {
        if(this.routePacket(packet))
            queue.removeIf(packetElement -> packetElement.getCorrelationId() == packet.getCorrelationId());
    }

    synchronized public boolean routePacket(Message packet) {
        var connection = connections.poll();
        if (connection == null) return false;
        sendPacket(connection, packet);
        connections.add(connection);
        return true;
    }

    public void addPacket(@NotNull Message packet) {
        SerializablePayload payload = packet.deserializePayload();
        if(payload instanceof AcknowledgementPacket ackPacket) {
            sentPackets.removeIf(toSendElement -> toSendElement.getCorrelationId() == ackPacket.correlationId);
            return;
        } else if (payload instanceof ErrorAcknowledgementPacket errPacket) {
            var index = IntStream.range(0, sentPackets.size())
                    .filter(i -> sentPackets.get(i).getCorrelationId() == errPacket.correlationId)
                    .findFirst();
            if (index.isPresent()) {
                var message = sentPackets.remove(index.getAsInt());
                addPacket(message);
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
        connections.remove(connection);
    }

    private void sendPacket(CatBaseConnection connection, Message packet) {
        sentPackets.add(packet);
        connection.sendPacket(packet);
    }

    public void shutdown() {
        queue.clear();
        sentPackets.clear();
        packetRedirectThread.shutdown();
    }

    public String getName() {
        return name;
    }
}
