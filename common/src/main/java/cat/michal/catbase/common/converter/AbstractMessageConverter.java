package cat.michal.catbase.common.converter;

import cat.michal.catbase.common.message.Message;

public abstract class AbstractMessageConverter<T> {
    public abstract T decode(Message message) throws Exception;

    public abstract Message encode(T object) throws Exception;
}
