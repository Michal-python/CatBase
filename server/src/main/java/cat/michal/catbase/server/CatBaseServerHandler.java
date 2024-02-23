package cat.michal.catbase.server;

import cat.michal.catbase.common.exception.CatBaseException;
import cat.michal.catbase.common.model.CatBaseConnection;
import cat.michal.catbase.server.event.impl.ConnectionEndEvent;
import cat.michal.catbase.server.event.EventDispatcher;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CatBaseServerHandler implements Runnable {
    private final CatBaseServer catBase;
    private final CatBaseConnection client;
    private final BufferedReader inputStream;
    private final EventDispatcher eventDispatcher;

    public CatBaseServerHandler(@NotNull CatBaseConnection client, CatBaseServer catBase, EventDispatcher eventDispatcher) {
        this.client = client;
        this.catBase = catBase;
        this.eventDispatcher = eventDispatcher;
        try {
            this.inputStream = new BufferedReader(new InputStreamReader(client.getSocket().getInputStream()));
        } catch (IOException e) {
            throw new CatBaseException(e);
        }
    }

    @Override
    public void run() {
        while (true) {
            if(client.getSocket().isClosed()) {
                Thread.currentThread().interrupt();
                eventDispatcher.dispatch(new ConnectionEndEvent(client), ConnectionEndEvent.class);
                break;
            }
            try {
                String message = inputStream.readLine();
                catBase.broadcastMessage(message);
            } catch (IOException ignored) {
            }
        }
    }
}
