package cat.michal.catbase.common.message;

import java.util.UUID;

public record Message(MessageProperties properties, byte[] payload, UUID correlationId, long packetId,
                      String routingKey, String exchangeName) {
    public Message(MessageProperties properties, byte[] payload, UUID correlationId, long packetId, String routingKey, String exchangeName) {
        this.properties = properties;
        this.payload = payload;
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
        this.packetId = packetId;
        this.correlationId = correlationId == null ? UUID.randomUUID() : correlationId;
    }
}
