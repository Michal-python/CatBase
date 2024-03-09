package cat.michal.catbase.common.packet.serverBound;

import cat.michal.catbase.common.packet.SerializablePayload;

import java.util.UUID;

public class ErrorAcknowledgementPacket implements SerializablePayload {
    public UUID correlationId;
}
