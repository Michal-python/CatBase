package cat.michal.catbase.server;

import cat.michal.catbase.common.exception.CatBaseException;
import cat.michal.catbase.common.message.Message;
import cat.michal.catbase.common.model.CatBaseConnection;
import cat.michal.catbase.common.model.CommunicationHeader;
import cat.michal.catbase.server.event.impl.ConnectionEndEvent;
import cat.michal.catbase.server.event.EventDispatcher;
import cat.michal.catbase.server.exchange.Exchange;
import cat.michal.catbase.server.procedure.ProcedureRegistry;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CatBaseServerHandler implements Runnable {
    private final CatBaseServer catBase;
    private static final Logger logger = Logger.getLogger(CatBaseServerHandler.class.getName());
    private final CatBaseConnection client;
    private final BufferedInputStream inputStream;
    private final EventDispatcher eventDispatcher;

    public CatBaseServerHandler(@NotNull CatBaseConnection client, CatBaseServer catBase, EventDispatcher eventDispatcher) {
        this.client = client;
        this.catBase = catBase;
        this.eventDispatcher = eventDispatcher;
        try {
            this.inputStream = new BufferedInputStream(client.getSocket().getInputStream());
        } catch (IOException e) {
            throw new CatBaseException(e);
        }
    }

    public void endConnection() {
        Thread.currentThread().interrupt();
        eventDispatcher.dispatch(new ConnectionEndEvent(client), ConnectionEndEvent.class);
    }

    @Override
    public void run() {
        while (true) {
            if(client.getSocket().isClosed()) {
               endConnection();
               break;
            }
            try {
                ByteBuffer buffer = ByteBuffer.wrap(inputStream.readNBytes(12));

                CommunicationHeader communicationHeader = CommunicationHeader.create(buffer);

                if(communicationHeader == null) {
                    endConnection();
                    return;
                }

                Message message = new CBORMapper().reader().readValue(
                        new LimitInputStream(inputStream, communicationHeader.getLength()),
                        Message.class
                );

                Exchange exchange = ProcedureRegistry.EXCHANGE_DETERMINE_PROCEDURE.proceed(message);

                if(exchange == null) {
                    //Invalid packet
                    logger.log(Level.WARNING, "Got invalid packet exchange from " + client.getId());
                    endConnection();
                    return;
                }

                exchange.route(message);
            } catch (IOException exception) {
                endConnection();
                return;
            }
        }
    }
}
