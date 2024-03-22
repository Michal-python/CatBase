package cat.michal.catbase.client.message;

import cat.michal.catbase.common.message.Message;
import cat.michal.catbase.common.packet.SerializablePayload;
import cat.michal.catbase.common.packet.clientBound.ErrorPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorPacketHandler implements MessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(ErrorPacketHandler.class);
    @Override
    public MessageHandleResult handle(Message message) {
        SerializablePayload payload = message.deserializePayload();
        if(payload instanceof ErrorPacket errorPacket) {
            logger.warn("Got error packet from CID: %s (%s, %s)".formatted(
                    message.getCorrelationId(),
                    errorPacket.getType().name(),
                    errorPacket.getMessage()
            ));
            return MessageHandleResult.shouldRespond(true);
        }

        return MessageHandleResult.shouldRespond(false);
    }

    @Override
    public long regardingPacketId() {
        return 0x3;
    }
}
