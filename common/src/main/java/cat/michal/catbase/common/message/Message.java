package cat.michal.catbase.common.message;

import cat.michal.catbase.common.packet.PacketType;
import cat.michal.catbase.common.packet.SerializablePayload;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;

import java.io.IOException;
import java.util.*;


@SuppressWarnings("unused")
public final class Message {

    public static final long USER_MESSAGE_ID = 0b0000111100001111000011110000111100001111000011110000111100001111L;

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
    private Map<String, Object> headers;

    /**
     * Needed for cbor
     */
    public Message() {}

    public Message(byte[] payload) {
        this(payload, null, null);
    }

    public Message(byte[] payload, String routingKey, String exchangeName) {
        this(payload, UUID.randomUUID(), USER_MESSAGE_ID, routingKey, exchangeName);
    }

    public Message(byte[] payload, UUID correlationId, long packetId, String routingKey, String exchangeName) {
        this.payload = payload;
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
        this.packetId = packetId;
        this.isResponse = false;
        this.correlationId = correlationId == null ? UUID.randomUUID() : correlationId;
        this.headers = new HashMap<>();
    }

    public Message setResponse(boolean val) {
        this.isResponse = val;
        return this;
    }

    public Message setShouldRespond(boolean val) {
        this.shouldRespond = val;
        return this;
    }

    public Message withHeaders(Map<String, Object> val) {
        this.headers.clear();

        this.headers.putAll(val);
        return this;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    public Message setOriginQueue(String queueName) {
        this.originQueue = queueName;
        return this;
    }

    public void setCorrelationId(UUID correlationId) {
        this.correlationId = correlationId;
    }

    public boolean isShouldRespond() {
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

    public Map<String, Object> getHeaders() {
        return headers;
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
        return isResponse == message.isResponse && shouldRespond == message.shouldRespond && packetId == message.packetId && Arrays.equals(payload, message.payload) && Objects.equals(correlationId, message.correlationId) && Objects.equals(routingKey, message.routingKey) && Objects.equals(exchangeName, message.exchangeName) && Objects.equals(originQueue, message.originQueue) && Objects.equals(headers, message.headers);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(correlationId, isResponse, shouldRespond, packetId, routingKey, exchangeName, originQueue);
        result = 31 * result + Arrays.hashCode(payload);
        return result;
    }

    @Override
    public String toString() {
        return String.format("M{id=%s correlationId=%s route=\"%s\" exchange=\"%s\" shouldRespond=%b payload=\"%s\"}",
                PacketType.findType(this.packetId).map(String::valueOf).orElseGet(() -> String.valueOf(this.packetId)),
                this.correlationId.toString(),
                this.routingKey,
                this.exchangeName,
                this.shouldRespond,
                new String(this.payload)
        );
    }
}
