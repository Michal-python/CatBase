package cat.michal.catbase.server.procedure;

import cat.michal.catbase.common.message.Message;
import cat.michal.catbase.common.packet.SerializablePayload;
import cat.michal.catbase.common.packet.serverBound.HandshakePacket;
import cat.michal.catbase.server.CatBaseServerHandler;
import cat.michal.catbase.server.auth.User;
import cat.michal.catbase.server.auth.UserRegistry;

import java.util.Optional;

public class InternalMessageProcedure implements BiProcedure<Boolean, Message, CatBaseServerHandler> {
    @Override
    public Boolean proceed(Message arg, CatBaseServerHandler connection) {
        SerializablePayload payload = arg.deserializePayload();
        if(payload instanceof HandshakePacket handshakePacket) {
            Optional<User> userOptional = UserRegistry.getUsers().stream()
                    .filter(userElement -> userElement.login().equals(handshakePacket.login))
                    .findFirst();
            if(userOptional.isEmpty()) {
                connection.endConnection();
                return true;
            }
            User user = userOptional.get();

            if(!UserRegistry.encodePassword(handshakePacket.password).equals(user.password())) {
                connection.endConnection();
                return true;
            }

            connection.verify();
        }
        return false;
    }
}