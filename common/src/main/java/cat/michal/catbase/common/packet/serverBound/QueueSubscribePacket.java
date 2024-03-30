package cat.michal.catbase.common.packet.serverBound;

import cat.michal.catbase.common.packet.SerializablePayload;

public class QueueSubscribePacket implements SerializablePayload {
    public String queueName;

    public QueueSubscribePacket(String queueName) {
        this.queueName = queueName;
    }

    @SuppressWarnings("unused")
    public QueueSubscribePacket() {}
}
