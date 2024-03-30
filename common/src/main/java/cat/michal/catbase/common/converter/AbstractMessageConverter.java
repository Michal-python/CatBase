package cat.michal.catbase.common.converter;

public abstract class AbstractMessageConverter<T> {
    public abstract T decode(byte[] message) throws Exception;

    public abstract byte[] encode(T object) throws Exception;
}
