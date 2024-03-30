package cat.michal.catbase.client.message;

import cat.michal.catbase.client.CatBaseClient;
import cat.michal.catbase.client.CatBaseClientConnection;
import cat.michal.catbase.common.message.Message;
import cat.michal.catbase.common.packet.SerializablePayload;
import cat.michal.catbase.common.packet.serverBound.AcknowledgementPacket;

public class AcknowledgementHandler implements MessageHandler {
    private final CatBaseClientConnection clientConnection;
    private final CatBaseClient client;

    public AcknowledgementHandler(CatBaseClientConnection clientConnection, CatBaseClient client) {
        this.clientConnection = clientConnection;
        this.client = client;
    }

    @Override
    public MessageHandleResult handle(Message message) {
        SerializablePayload payload = message.deserializePayload();
        if(payload instanceof AcknowledgementPacket) {
            clientConnection.awaitingForAck().removeIf(ack -> ack.equals(message.getCorrelationId()));
            client.getReceivedAcknowledgements().stream()
                    .filter(receive -> receive.correlationId().equals(message.getCorrelationId()))
                    .findAny().ifPresent(present -> present.consumer().accept(null));

            return MessageHandleResult.shouldRespond(true);
        }
        return MessageHandleResult.shouldRespond(false);
    }

    @Override
    public long regardingPacketId() {
        return 0x2;
    }
}
