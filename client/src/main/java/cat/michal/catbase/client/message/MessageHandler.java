package cat.michal.catbase.client.message;

import cat.michal.catbase.common.message.Message;

public interface MessageHandler {
    MessageHandleResult handle(Message message);

    long regardingPacketId();
}
