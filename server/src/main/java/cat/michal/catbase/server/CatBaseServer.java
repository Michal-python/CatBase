package cat.michal.catbase.server;

import cat.michal.catbase.common.exception.CatBaseException;
import cat.michal.catbase.common.model.CatBaseConnection;
import cat.michal.catbase.common.data.ListKeeper;
import cat.michal.catbase.server.event.EventDispatcher;
import cat.michal.catbase.server.event.impl.ConnectionEstablishEvent;
import cat.michal.catbase.server.procedure.ProcedureRegistry;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class CatBaseServer implements BaseServer {
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
                if (catBaseConnection == null) {
                    return;
                }
                connections.add(catBaseConnection);
                eventDispatcher.dispatch(new ConnectionEstablishEvent(catBaseConnection));

                new Thread(new CatBaseServerCommunicationThread(catBaseConnection, eventDispatcher)).start();
            }
        }
    }

    public int getPort() {
        return port;
    }

    @Override
    public void stopServer() {
        try {
            this.running = false;
            if(serverSocket != null) {
                serverSocket.close();
            }
            this.serverSocket = null;

            //general cleanup
            this.connections.clear();
            ListKeeper.getInstance().shutdown();
        } catch (IOException e) {
            throw new CatBaseException(e);
        }
    }
}
