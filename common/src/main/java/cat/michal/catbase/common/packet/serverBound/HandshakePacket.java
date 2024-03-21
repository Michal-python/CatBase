package cat.michal.catbase.common.packet.serverBound;


public class HandshakePacket implements AuthorizationPacket {
    public String login;
    public String password;

    public HandshakePacket(String login, String password) {
        this.login = login;
        this.password = password;
    }
}
