package cat.michal.catbase.common.packet.serverBound;

import cat.michal.catbase.common.packet.SerializablePayload;

public class AcknowledgementPacket implements SerializablePayload {
    public boolean shouldRespond;

    public AcknowledgementPacket(boolean shouldRespond) {
        this.shouldRespond = shouldRespond;
    }

    @SuppressWarnings("unused")
    public AcknowledgementPacket() {
    }
}
