package cat.michal.catbase.common.packet.clientBound;

import cat.michal.catbase.common.packet.ErrorType;
import cat.michal.catbase.common.packet.SerializablePayload;

public class ErrorPacket implements SerializablePayload {
    private ErrorType type;
    private String message;

    public ErrorPacket(ErrorType type, String message) {
        this.type = type;
        this.message = message;
    }
}
