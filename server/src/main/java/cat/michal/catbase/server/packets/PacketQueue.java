package cat.michal.catbase.server.packets;

import cat.michal.catbase.common.data.ListKeeper;
import cat.michal.catbase.common.data.TimedList;
import cat.michal.catbase.common.message.Message;
import cat.michal.catbase.common.model.CatBaseConnection;
import cat.michal.catbase.common.packet.ErrorType;
import cat.michal.catbase.common.packet.SerializablePayload;
import cat.michal.catbase.common.packet.clientBound.ErrorPacket;
import cat.michal.catbase.common.packet.serverBound.AcknowledgementPacket;
import cat.michal.catbase.common.packet.serverBound.ErrorAcknowledgementPacket;
import cat.michal.catbase.server.event.EventDispatcher;
import cat.michal.catbase.server.event.impl.ConnectionEndEvent;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Consumer;

public abstract class PacketQueue {
    private final String name;
    protected Queue<Message> queue;
    protected final TimedList<ReceivedMessage> receivedMessages = ListKeeper.getInstance().createDefaultTimeList();
    protected final TimedList<SentMessage> sentPackets = ListKeeper.getInstance().createDefaultTimeList();
    protected final Queue<CatBaseConnection> connections = new LinkedList<>();
    private final PacketRedirectThread packetRedirectThread;

    private final Consumer<ConnectionEndEvent> disconnectionListener;

    public PacketQueue(int queueCapacity, String name) {
        this.name = name;
        this.packetRedirectThread = new PacketRedirectThread(this);
        this.queue = new ArrayBlockingQueue<>(queueCapacity);
        this.disconnectionListener = (e) -> unsubscribe(e.getConnection());
        EventDispatcher.getInstance().hook(ConnectionEndEvent.class, this.disconnectionListener);
        this.packetRedirectThread.start();
    }

    /**
     * Redirects packet to packet queue listeners
     *
     * @param packet packet to redirect
     */
    public void redirectPacket(Message packet) {
        if (this.routePacket(packet))
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

    public void addPacket(@NotNull Message packet, CatBaseConnection origin) {
        SerializablePayload payload = packet.deserializePayload();

        if (payload instanceof AcknowledgementPacket ackPacket) {
            if (!ackPacket.shouldRespond) {
                sentPackets.removeIf(toSendElement -> toSendElement.message.getCorrelationId() == packet.getCorrelationId());
            }
            return;
        } else if (payload instanceof ErrorAcknowledgementPacket errPacket) {
            var sentMessage = sentPackets.findAndRemove(e -> e.message.getCorrelationId() == errPacket.correlationId);
            if (sentMessage != null) {
                addPacket(sentMessage.message, sentMessage.connection.get());
            }
            return;
        }

        if (packet.isResponse()) {
            var packetElement = receivedMessages.findAndRemove(element -> element.message.getCorrelationId().equals(packet.getCorrelationId()));
            if (packetElement != null) {
                if (packetElement.message.shouldRespond()) {
                    CatBaseConnection requesterConnection = packetElement.connection.get();
                    if (requesterConnection == null || !requesterConnection.sendPacket(packet.setOriginQueue(this.name))) {
                        origin.sendError(new ErrorPacket(ErrorType.UNSPECIFIED, "The sender is long gone!"), packet);
                    }
                    receivedMessages.removeIf(msg -> msg.message.getCorrelationId().equals(packet.getCorrelationId()));
                } else {
                    origin.sendError(new ErrorPacket(ErrorType.UNSPECIFIED, "The sender did not expect a response"), packet);
                }
            } else {
                origin.sendError(new ErrorPacket(ErrorType.UNSPECIFIED, "Nothing to respond to!"), packet);
            }
            return;
        }

        if (packet.shouldRespond()) {
            receivedMessages.add(new ReceivedMessage(packet, new WeakReference<>(origin)));
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
            }
            return false;
        });
    }

    private boolean sendPacket(CatBaseConnection connection, Message packet) {
        packet.setOriginQueue(this.name);
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
    ) {
    }

    public record ReceivedMessage(
            Message message,
            WeakReference<CatBaseConnection> connection
    ) {
    }
}
