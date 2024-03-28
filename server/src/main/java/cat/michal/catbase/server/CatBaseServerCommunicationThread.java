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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

public class CatBaseServerCommunicationThread implements Runnable {
    private final CatBaseConnection client;
    private final EventDispatcher eventDispatcher;
    private volatile boolean verified = false;

    public CatBaseServerCommunicationThread(@NotNull CatBaseConnection client, EventDispatcher eventDispatcher) {
        this.client = client;
        this.eventDispatcher = eventDispatcher;
    }

    public CatBaseConnection getClient() {
        return client;
    }

    public void verify() {
        this.verified = true;
    }

    public void endConnection() {
        try {
            client.close();
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

        while (true) {
            if (!readIncomingMessage()) {
                return;
            }
        }
    }

    public boolean readIncomingMessage() {
        try {
            Message message = client.readMessage();

            if (ProcedureRegistry.INTERNAL_MESSAGE_PROCEDURE.proceed(message, this)) {
                return false;
            }

            Exchange exchange = ProcedureRegistry.EXCHANGE_DETERMINE_PROCEDURE.proceed(message);

            if (exchange == null) {
                client.sendError(
                        new ErrorPacket(ErrorType.EXCHANGE_NOT_FOUND, "Exchange '" + message.getExchangeName() + "' was not found"),
                        message
                );
                return true;
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
