package cat.michal.catbase.common.converter;

import cat.michal.catbase.common.message.Message;

public class StringMessageConverter extends AbstractMessageConverter<String> {
    @Override
    public String decode(Message message) {
        return new String(message.getPayload());
    }

    @Override
    public Message encode(String object) {
        return new Message(object.getBytes());
    }
}
