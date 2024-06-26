package cat.michal.catbase.common.packet;

import cat.michal.catbase.common.packet.clientBound.ErrorPacket;
import cat.michal.catbase.common.packet.serverBound.*;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;

public enum PacketType {
    HANDSHAKE(HandshakePacket.class, 0x01),
    ACKNOWLEDGEMENT_PACKET(AcknowledgementPacket.class, 0x02),
    ERROR_PACKET(ErrorPacket.class, 0x03),
    SUBSCRIBE(QueueSubscribePacket.class, 0x04),
    UNSUBSCRIBE(QueueUnsubscribePacket.class, 0x05),
    ERROR_ACKNOWLEDGEMENT_PACKET(ErrorAcknowledgementPacket.class, 0x06);

    private final Class<? extends SerializablePayload> clazz;
    private final long id;
    PacketType(Class<? extends SerializablePayload> clazz, long id) {
        this.clazz = clazz;
        this.id = id;
    }

    public static @NotNull Optional<PacketType> findType(long id) {
        return Arrays.stream(values())
                .filter(type -> type.getId() == id)
                .findFirst();
    }

    public Class<? extends SerializablePayload> getClazz() {
        return clazz;
    }

    public long getId() {
        return id;
    }
}
