package cat.michal.catbase;

import cat.michal.catbase.exception.CatBaseException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CatBase implements BaseServer {
    private final Map<String, JSONObject> database;
    private boolean running;
    private final ServerSocket serverSocket;
    private final List<Socket> connections;

    public CatBase(int port) {
        this.database = new HashMap<>();
        this.connections = new ArrayList<>();
        this.running = true;
        try {
            this.serverSocket = new ServerSocket(port);
            this.serverSocket.setReuseAddress(true);
        } catch (IOException e) {
            throw new CatBaseException(e);
        }
    }

    @Override
    public void startServer() {
        while (running) {
            Socket connection;
            try {
                connection = serverSocket.accept();
            } catch (IOException e) {
                throw new CatBaseException(e);
            }
            System.out.println("[S] Nowe połączenie z " + connection.getRemoteSocketAddress());
            connections.add(connection);
            new Thread(new CatBaseServerHandler(connection, this)).start();
        }
    }

    @Override
    public void stopServer() {
        try {
            serverSocket.close();
            this.running = false;
        } catch (IOException e) {
            throw new CatBaseException(e);
        }
    }

    public synchronized void sendMessage(String message) {
        for (Socket connection : connections) {
            if (connection.isClosed()) {
                this.connections.remove(connection);
                continue;
            }
            try (var printWriter = new PrintWriter(connection.getOutputStream(), true)) {
                printWriter.println(message);
            } catch (IOException e) {
                throw new CatBaseException(e);
            }
        }
    }

    void removeConnection(Socket client) {
        this.connections.remove(client);
    }
}
