package cat.michal.catbase.common.converter;

import cat.michal.catbase.common.message.Message;

public class DefaultMessageConverter extends AbstractMessageConverter<byte[]> {
    @Override
    public byte[] decode(Message message) {
        return message.getPayload();
    }

    @Override
    public Message encode(byte[] object) {
        return new Message(object);
    }
}
