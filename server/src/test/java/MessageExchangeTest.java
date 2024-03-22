import cat.michal.catbase.common.message.Message;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MessageExchangeTest {

    @Test
    void pureDataTest() throws IOException {
        CBORMapper cbor = new CBORMapper();
        cbor.getFactory().disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        Message message1 = new Message("Hello".getBytes(), UUID.randomUUID(), 0, "a", "b");
        Message message2 = new Message("World".getBytes(), UUID.randomUUID(), 1, "c", "d");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        cbor.writer().writeValue(outputStream, message1);
        cbor.writer().writeValue(outputStream, message2);


        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        CBORFactory cborFactory = new CBORFactory();
        ObjectMapper mapper = new ObjectMapper(cborFactory);

        mapper.getFactory().disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);

        JsonParser parser = cborFactory.createParser(inputStream);
        Message message3 = mapper.readValue(parser, Message.class);
        Message message4 = mapper.readValue(parser, Message.class);
        assertEquals(message1, message3);
        assertEquals(message2, message4);

    }

}
