package cat.michal.catbase.common.message;

import cat.michal.catbase.common.packet.PacketType;
import cat.michal.catbase.common.packet.SerializablePayload;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class Message {
    private static final ObjectReader cborMapper = new CBORMapper().reader();
    private byte[] payload;
    private UUID correlationId;
    private boolean isResponse;
    private boolean shouldRespond;
    private long packetId;
    private String routingKey;
    private String exchangeName;
    /**
     * If this message is either a response, or being received by a consumer,
     * this field indicates the origin queue; otherwise it is null
     */
    private String originQueue;

    /**
     * Needed for cbor
     */
    public Message() {}

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return isResponse == message.isResponse && shouldRespond == message.shouldRespond && packetId == message.packetId && Arrays.equals(payload, message.payload) && Objects.equals(correlationId, message.correlationId) && Objects.equals(routingKey, message.routingKey) && Objects.equals(exchangeName, message.exchangeName) && Objects.equals(originQueue, message.originQueue);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(correlationId, isResponse, shouldRespond, packetId, routingKey, exchangeName, originQueue);
        result = 31 * result + Arrays.hashCode(payload);
        return result;
    }
}
