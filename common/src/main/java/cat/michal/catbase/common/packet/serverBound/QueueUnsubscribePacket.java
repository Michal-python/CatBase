package cat.michal.catbase.common.packet.serverBound;

import cat.michal.catbase.common.packet.SerializablePayload;

public class QueueUnsubscribePacket implements SerializablePayload {
    public String queueName;

    public QueueUnsubscribePacket(String queueName) {
        this.queueName = queueName;
    }

    @SuppressWarnings("unused")
    public QueueUnsubscribePacket() {}
}
