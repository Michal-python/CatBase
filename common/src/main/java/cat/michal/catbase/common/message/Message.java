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
    private final byte[] payload;
    private final UUID correlationId;
    private boolean isResponse;
    private boolean shouldRespond;
    private final long packetId;
    private final String routingKey;
    private final String exchangeName;
    /**
     * If this message is either a response, or being received by a consumer,
     * this field indicates the origin queue; otherwise it is null
     */
    private String originQueue;

    public Message(byte[] payload, UUID correlationId, long packetId, String routingKey, String exchangeName) {
        this.payload = payload;
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
        this.packetId = packetId;
        this.isResponse = false;
        this.correlationId = correlationId == null ? UUID.randomUUID() : correlationId;
    }

    public Message setResponse(boolean val) {
        this.isResponse = val;
        return this;
    }

    public Message setShouldRespond(boolean val) {
        this.shouldRespond = val;
        return this;
    }

    public Message setOriginQueue(String queueName) {
        this.originQueue = queueName;
        return this;
    }

    public boolean shouldRespond() {
        return shouldRespond;
    }

    public boolean isResponse() {
        return isResponse;
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

    public String getOriginQueue() {
        return originQueue;
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
