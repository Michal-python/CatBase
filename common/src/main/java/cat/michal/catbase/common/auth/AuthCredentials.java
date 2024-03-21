package cat.michal.catbase.common.auth;

import cat.michal.catbase.common.packet.serverBound.AuthorizationPacket;

public interface AuthCredentials {
    CredentialsType getType();

    AuthorizationPacket wrapCredentials();
}
