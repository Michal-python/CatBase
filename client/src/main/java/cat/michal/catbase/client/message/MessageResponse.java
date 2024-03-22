package cat.michal.catbase.client.message;

public final class MessageResponse {
    private byte[] payload;
    private long packetId;

    public MessageResponse(byte[] payload, long packetId) {
        this.payload = payload;
        this.packetId = packetId;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public long getPacketId() {
        return packetId;
    }

    public void setPacketId(long packetId) {
        this.packetId = packetId;
    }
}
