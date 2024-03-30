package cat.michal.catbase.common.converter;

public class DefaultMessageConverter extends AbstractMessageConverter<byte[]> {
    @Override
    public byte[] decode(byte[] message) {
        return message;
    }

    @Override
    public byte[] encode(byte[] object) {
        return object;
    }
}
