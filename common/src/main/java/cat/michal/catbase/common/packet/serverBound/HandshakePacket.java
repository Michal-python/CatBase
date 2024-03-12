package cat.michal.catbase.common.packet.serverBound;

import cat.michal.catbase.common.packet.SerializablePayload;

public class HandshakePacket implements SerializablePayload {
    public String login;
    public String password;

    public HandshakePacket(String login, String password) {
        this.login = login;
        this.password = password;
    }
}
