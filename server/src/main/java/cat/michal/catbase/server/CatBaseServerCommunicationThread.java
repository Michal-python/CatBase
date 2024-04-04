package cat.michal.catbase.server;

import cat.michal.catbase.common.message.Message;
import cat.michal.catbase.common.model.CatBaseConnection;
import cat.michal.catbase.common.packet.ErrorType;
import cat.michal.catbase.common.packet.clientBound.ErrorPacket;
import cat.michal.catbase.injector.Injector;
import cat.michal.catbase.server.event.EventDispatcher;
import cat.michal.catbase.server.event.impl.ConnectionEndEvent;
import cat.michal.catbase.server.exchange.Exchange;
import cat.michal.catbase.server.procedure.ExchangeDetermineProcedure;
import cat.michal.catbase.server.procedure.InternalMessageProcedure;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class CatBaseServerCommunicationThread implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(CatBaseServerCommunicationThread.class);
    private final CatBaseConnection client;
    private final EventDispatcher eventDispatcher;
    private final Injector procedureInjector;
    private volatile boolean verified = false;

    public CatBaseServerCommunicationThread(@NotNull CatBaseConnection client, EventDispatcher eventDispatcher, Injector procedureInjector) {
        this.client = client;
        this.eventDispatcher = eventDispatcher;
        this.procedureInjector = procedureInjector;
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
                logger.debug("Comm with client ended " + client.getId() + " ");
                return;
            }
        }
    }

    public boolean readIncomingMessage() {
        if (!client.isOpen()) {
            return false;
        }

        try {
            Message message = client.readMessage();

            logger.debug("Packet received {} from client {}", message.toString(), client.getId());

            switch (procedureInjector.getInstance(InternalMessageProcedure.class).proceed(message, this)) {
                case CONTINUE -> { }
                case DO_NOT_CONTINUE -> {
                    return true;
                }
                case AUTH_ERROR -> {
                    return false;
                }
            }

            Exchange exchange = procedureInjector.getInstance(ExchangeDetermineProcedure.class).proceed(message);

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
            logger.debug("Exception from client {} ", client.getId(), exception);
            return false;
        }

        return true;
    }

}
