package cat.michal.catbase.common.auth;

import cat.michal.catbase.common.packet.serverBound.AuthorizationPacket;
import cat.michal.catbase.common.packet.serverBound.HandshakePacket;

public class PasswordCredentials implements AuthCredentials {
    public String login;
    public String password;

    @Override
    public AuthorizationPacket wrapCredentials() {
        return new HandshakePacket(login, password);
    }

    @Override
    public CredentialsType getType() {
        return CredentialsType.PASSWORD;
    }
}
