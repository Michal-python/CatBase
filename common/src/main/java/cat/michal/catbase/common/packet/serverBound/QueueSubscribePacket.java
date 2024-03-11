package cat.michal.catbase.common.packet.serverBound;

import cat.michal.catbase.common.packet.SerializablePayload;

public class QueueSubscribePacket implements SerializablePayload {
    public String queueName;
}
