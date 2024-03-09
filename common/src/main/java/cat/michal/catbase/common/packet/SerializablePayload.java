package cat.michal.catbase.common.packet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;

public interface SerializablePayload {
    ObjectWriter cborWriter = new CBORMapper().writer();

    default byte[] serialize() throws JsonProcessingException {
        return cborWriter.writeValueAsBytes(this);
    }
}
