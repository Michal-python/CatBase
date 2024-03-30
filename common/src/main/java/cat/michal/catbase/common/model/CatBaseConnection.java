package cat.michal.catbase.common.model;

import cat.michal.catbase.common.message.Message;
import cat.michal.catbase.common.packet.PacketType;
import cat.michal.catbase.common.packet.clientBound.ErrorPacket;
import cat.michal.catbase.common.packet.serverBound.AcknowledgementPacket;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

public class CatBaseConnection {

    private final UUID id;
    private final Socket socket;
    private JsonParser parser;

    private final ReentrantLock writeLock = new ReentrantLock();
    private final ReentrantLock readLock = new ReentrantLock();

    public CatBaseConnection(UUID id, Socket socket) throws IOException {
        this.id = id;
        this.socket = socket;
    }

    private static final ThreadLocal<ObjectMapper> cborMapper = new ThreadLocal<>() {
        @Override
        public ObjectMapper get() {
            return new ObjectMapper(cborFactory.get());
        }
    };

    private static final Logger logger = LoggerFactory.getLogger(CatBaseConnection.class);

    private static final ThreadLocal<CBORFactory> cborFactory = new ThreadLocal<>() {
        @Override
        public CBORFactory get() {
            var factory = new CBORFactory();
            factory.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
            factory.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
            return factory;
        }
    };


    public void sendError(@NotNull ErrorPacket error, Message reference) {
        try {
            sendPacket(new Message(
                    error.serialize(),
                    reference.getCorrelationId(),
                    PacketType.ERROR_PACKET.getId(),
                    null,
                    null
            ));
        } catch (JsonProcessingException ignored) {
        }
    }

    public void sendAcknowledgement(@NotNull Message reference) {
        try {
            sendPacket(new Message(
                    new AcknowledgementPacket(false).serialize(),
                    reference.getCorrelationId(),
                    PacketType.ACKNOWLEDGEMENT_PACKET.getId(),
                    null,
                    null
            ));
        } catch (JsonProcessingException ignored) {
        }
    }

    public boolean isOpen() {
        return this.socket != null && this.socket.isConnected() && !this.socket.isClosed();
    }

    public void close() throws IOException {
        this.socket.close();
        if (this.parser != null) {
            this.parser.close();
        }
    }

    public Message readMessage() throws IOException {
        try {
            readLock.lock();
            if (parser == null) {
                this.parser = cborFactory.get().createParser(socket.getInputStream());
            }
            return cborMapper.get().readValue(parser, Message.class);
        } finally {
            readLock.unlock();
        }
    }

    public UUID getId() {
        return id;
    }

    public boolean sendPacket(Message packet) {
        try {
            writeLock.lock();
            cborMapper.get().writeValue(socket.getOutputStream(), packet);
            logger.debug("Packet sent " + packet.toString());
            return true;
        } catch (IOException exception) {
            logger.debug("Got exception while sending packet ", exception);
            return false;
        } finally {
            writeLock.unlock();
        }
    }


    public Socket getSocket() {
        return this.socket;
    }
}
