package cat.michal.catbase.client.message;

import cat.michal.catbase.client.CatBaseClient;
import cat.michal.catbase.common.message.Message;
import cat.michal.catbase.common.packet.SerializablePayload;
import cat.michal.catbase.common.packet.clientBound.ErrorPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorPacketHandler extends MessageHandler<Object> {
    private static final Logger logger = LoggerFactory.getLogger(ErrorPacketHandler.class);
    private final CatBaseClient client;

    public ErrorPacketHandler(CatBaseClient client) {
        this.client = client;
    }

    @Override
    public MessageHandleResult handle(Message message, Object ignored) {
        SerializablePayload payload = message.deserializePayload();
        if(payload instanceof ErrorPacket errorPacket) {
            logger.debug("Got error packet from CID: %s (%s, %s)".formatted(
                    message.getCorrelationId(),
                    errorPacket.getType().name(),
                    errorPacket.getMessage()
            ));
            client.getReceivedAcknowledgements().stream()
                    .filter(receive -> receive.correlationId().equals(message.getCorrelationId()))
                    .findAny().ifPresent(present -> present.consumer().accept(errorPacket));

            return MessageHandleResult.shouldRespond(true);
        }

        return MessageHandleResult.shouldRespond(false);
    }

    @Override
    public long regardingPacketId() {
        return 0x3;
    }
}
