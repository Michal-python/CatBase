package cat.michal.catbase.common.converter;

public class StringMessageConverter extends AbstractMessageConverter<String> {
    @Override
    public String decode(byte[] message) {
        return new String(message);
    }

    @Override
    public byte[] encode(String object) {
        return object.getBytes();
    }
}
