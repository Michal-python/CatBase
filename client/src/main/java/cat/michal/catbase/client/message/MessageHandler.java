package cat.michal.catbase.client.message;

import cat.michal.catbase.common.message.Message;

public abstract class MessageHandler<T> {

    public abstract MessageHandleResult handle(Message message, T payload);

    public String queue() {
        return null;
    }

    public long regardingPacketId() {
        return Message.USER_MESSAGE_ID;
    }
}
