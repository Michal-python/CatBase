package cat.michal.catbase.server;

import cat.michal.catbase.common.exception.CatBaseException;
import cat.michal.catbase.common.model.CatBaseConnection;
import cat.michal.catbase.server.event.impl.ConnectionEstablishEvent;
import cat.michal.catbase.server.event.EventDispatcher;
import cat.michal.catbase.server.procedure.ProcedureRegistry;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CatBaseServer implements BaseServer {
    private static final Logger logger = Logger.getLogger(CatBaseServer.class.getName());
    private static final EventDispatcher eventDispatcher = new EventDispatcher();
    private boolean running;
    private final int port;
    private ServerSocket serverSocket;
    private final List<CatBaseConnection> connections;

    public CatBaseServer(int port) {
        this.connections = new ArrayList<>();
        this.running = false;
        this.port = port;
    }

    @Override
    public void startServer() {
        this.running = true;
        try {
            this.serverSocket = new ServerSocket(this.port);
            this.serverSocket.setReuseAddress(true);
        } catch (IOException e) {
            throw new CatBaseException(e);
        }

        logger.log(Level.INFO, "Started server on port " + this.port);

        while (this.running) {
            Socket connection = null;

            try {
                connection = this.serverSocket.accept();
            } catch (IOException e) {
                if(this.serverSocket != null && !this.serverSocket.isClosed()) {
                    throw new CatBaseException(e);
                }
            }

            if(connection != null) {
                CatBaseConnection catBaseConnection = ProcedureRegistry.CONNECTION_ESTABLISHMENT_PROCEDURE.proceed(connection);
                connections.add(catBaseConnection);
                eventDispatcher.dispatch(new ConnectionEstablishEvent(catBaseConnection), ConnectionEstablishEvent.class);

                new Thread(new CatBaseServerHandler(catBaseConnection, this, eventDispatcher)).start();
            }
        }
    }

    @Override
    public void stopServer() {
        try {
            this.running = false;
            serverSocket.close();
            this.serverSocket = null;

            //general cleanup
            this.connections.clear();

            logger.log(Level.INFO, "Server stopped");
        } catch (IOException e) {
            throw new CatBaseException(e);
        }
    }

    public synchronized void broadcastMessage(String message) {
        if(!running) {
            throw new CatBaseException("Cannot broadcast message while server is off");
        }

        for (CatBaseConnection connection : connections) {
            if (connection.getSocket().isClosed()) {
                this.connections.remove(connection);
                continue;
            }
            try (var printWriter = new PrintWriter(connection.getSocket().getOutputStream(), true)) {
                printWriter.println(message);
            } catch (IOException e) {
                throw new CatBaseException(e);
            }
        }
    }
}
