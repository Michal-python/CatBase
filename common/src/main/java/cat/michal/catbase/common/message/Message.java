package cat.michal.catbase.common.message;

import cat.michal.catbase.common.packet.PacketType;
import cat.michal.catbase.common.packet.SerializablePayload;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

public final class Message {
    private static final ObjectReader cborMapper = new CBORMapper().reader();
    private final MessageProperties properties;
    private final byte[] payload;
    private final UUID correlationId;
    private final long packetId;
    private final String routingKey;
    private final String exchangeName;

    public Message(MessageProperties properties, byte[] payload, UUID correlationId, long packetId, String routingKey, String exchangeName) {
        this.properties = properties;
        this.payload = payload;
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
        this.packetId = packetId;
        this.correlationId = correlationId == null ? UUID.randomUUID() : correlationId;
    }

    public MessageProperties getProperties() {
        return properties;
    }

    public byte[] getPayload() {
        return payload;
    }

    public UUID getCorrelationId() {
        return correlationId;
    }

    public long getPacketId() {
        return packetId;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public SerializablePayload deserializePayload() {
        Optional<PacketType> type = PacketType.findType(this.getPacketId());
        if(type.isEmpty()) {
            return null;
        }

        Class<? extends SerializablePayload> clazz = type.get().getClazz();
        try {
            return cborMapper.readValue(this.getPayload(), clazz);
        } catch (IOException exception) {
            return null;
        }
    }
}
