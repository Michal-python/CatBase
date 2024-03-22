package cat.michal.catbase.client.message;

import cat.michal.catbase.client.CatBaseClientConnection;
import cat.michal.catbase.common.message.Message;
import cat.michal.catbase.common.packet.SerializablePayload;
import cat.michal.catbase.common.packet.serverBound.AcknowledgementPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AcknowledgementHandler implements MessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(AcknowledgementHandler.class);
    private final CatBaseClientConnection clientConnection;

    public AcknowledgementHandler(CatBaseClientConnection clientConnection) {
        this.clientConnection = clientConnection;
    }

    @Override
    public MessageHandleResult handle(Message message) {
        SerializablePayload payload = message.deserializePayload();
        if(payload instanceof AcknowledgementPacket) {
            clientConnection.awaitingForAck().removeIf(
                    ack -> ack.getCorrelationId() == message.getCorrelationId()
            );
            return MessageHandleResult.shouldRespond(true);
        }
        return MessageHandleResult.shouldRespond(false);
    }

    @Override
    public long regardingPacketId() {
        return 0x2;
    }
}
