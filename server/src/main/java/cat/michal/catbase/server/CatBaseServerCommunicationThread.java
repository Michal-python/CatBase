package cat.michal.catbase.server;

import cat.michal.catbase.common.exception.CatBaseException;
import cat.michal.catbase.common.message.Message;
import cat.michal.catbase.common.model.CatBaseConnection;
import cat.michal.catbase.common.packet.ErrorType;
import cat.michal.catbase.common.packet.clientBound.ErrorPacket;
import cat.michal.catbase.server.event.EventDispatcher;
import cat.michal.catbase.server.event.impl.ConnectionEndEvent;
import cat.michal.catbase.server.exchange.Exchange;
import cat.michal.catbase.server.procedure.ProcedureRegistry;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

public class CatBaseServerCommunicationThread implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(CatBaseServerHandler.class);
    private static final CBORFactory cborFactory = new CBORFactory();
    private static final ObjectMapper cborMapper = new ObjectMapper(cborFactory);
    private final CatBaseConnection client;
    private final BufferedInputStream inputStream;
    private final EventDispatcher eventDispatcher;
    private JsonParser parser;
    private volatile boolean verified = false;

    static {
        cborFactory.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
    }

    public CatBaseServerCommunicationThread(@NotNull CatBaseConnection client, EventDispatcher eventDispatcher) {
        this.client = client;
        this.eventDispatcher = eventDispatcher;
        try {
            this.inputStream = new BufferedInputStream(client.socket().getInputStream());
        } catch (IOException e) {
            throw new CatBaseException(e);
        }
    }

    public CatBaseConnection getClient() {
        return client;
    }

    public void verify() {
        this.verified = true;
    }

    public void endConnection() {
        try {
            client.socket().close();
            parser.close();
        } catch (IOException ignored) {
        }
        Thread.currentThread().interrupt();
        eventDispatcher.dispatch(new ConnectionEndEvent(client));
    }

    @Override
    public void run() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (!verified) {
                    endConnection();
                }
            }
        }, 1001);

        try {
            parser = cborFactory.createParser(inputStream);
        } catch (IOException e) {
            endConnection();
        }

        while (true) {
            if (!readIncomingMessage(this.inputStream)) {
                return;
            }
        }
    }

    public boolean readIncomingMessage(@NotNull InputStream inputStream) {
        try {
            Message message = cborMapper.readValue(parser, Message.class);

            if (ProcedureRegistry.INTERNAL_MESSAGE_PROCEDURE.proceed(message, this)) {
                return false;
            }

            Exchange exchange = ProcedureRegistry.EXCHANGE_DETERMINE_PROCEDURE.proceed(message);

            if (exchange == null) {
                client.sendError(
                        new ErrorPacket(ErrorType.EXCHANGE_NOT_FOUND, "Exchange '" + message.getExchangeName() + "' was not found"),
                        message
                );
            }

            if (!exchange.route(message, this.getClient())) {
                client.sendError(
                        new ErrorPacket(ErrorType.QUEUE_NOT_FOUND, "Queue from routing key '" + message.getRoutingKey() + "' was not found"),
                        message
                );
            }
        } catch (IOException exception) {
            endConnection();
            return false;
        }

        return true;
    }

}
