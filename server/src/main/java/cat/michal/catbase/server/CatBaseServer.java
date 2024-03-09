package cat.michal.catbase.server;

import cat.michal.catbase.common.exception.CatBaseException;
import cat.michal.catbase.common.model.CatBaseConnection;
import cat.michal.catbase.server.event.impl.ConnectionEstablishEvent;
import cat.michal.catbase.server.event.EventDispatcher;
import cat.michal.catbase.server.procedure.ProcedureRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class CatBaseServer implements BaseServer {
    private static final Logger logger = LoggerFactory.getLogger(CatBaseServer.class);
    private static final EventDispatcher eventDispatcher = new EventDispatcher();
    private volatile boolean running;
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

        logger.info("Started server on port " + this.port);

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

                new Thread(new CatBaseServerHandler(catBaseConnection, eventDispatcher)).start();
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

            logger.info("Server stopped");
        } catch (IOException e) {
            throw new CatBaseException(e);
        }
    }
}
