package cat.michal.catbase.server.procedure;

import cat.michal.catbase.common.message.Message;
import cat.michal.catbase.common.packet.ErrorType;
import cat.michal.catbase.common.packet.SerializablePayload;
import cat.michal.catbase.common.packet.clientBound.ErrorPacket;
import cat.michal.catbase.common.packet.serverBound.HandshakePacket;
import cat.michal.catbase.common.packet.serverBound.QueueSubscribePacket;
import cat.michal.catbase.common.packet.serverBound.QueueUnsubscribePacket;
import cat.michal.catbase.server.CatBaseServerHandler;
import cat.michal.catbase.server.auth.User;
import cat.michal.catbase.server.auth.UserRegistry;
import cat.michal.catbase.server.exchange.ExchangeRegistry;

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
        if(payload instanceof QueueSubscribePacket queuePacket) {
            ExchangeRegistry.findQueue(queuePacket.queueName).ifPresentOrElse(queue -> {
                queue.subscribe(connection.getClient());
                connection.getClient().sendAcknowledgement(arg);
            }, () -> connection.getClient().sendError(
                    new ErrorPacket(ErrorType.QUEUE_NOT_FOUND, "Queue '" + queuePacket.queueName + "' was not found"),
                    arg
            ));
        }
        if(payload instanceof QueueUnsubscribePacket queueUnsubscribePacket) {
            ExchangeRegistry.findQueue(queueUnsubscribePacket.queueName).ifPresentOrElse(queue -> {
                queue.unsubscribe(connection.getClient());
                connection.getClient().sendAcknowledgement(arg);
            }, () -> connection.getClient().sendError(
                    new ErrorPacket(ErrorType.QUEUE_NOT_FOUND, "Queue '" + queueUnsubscribePacket.queueName + "' was not found"),
                    arg
            ));
        }
        return false;
    }
}