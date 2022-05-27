package cat.michal.catbase.client;

import cat.michal.catbase.exception.CatBaseException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class CatBaseClient {
    private Socket socket;

    private final String host;
    private final int port;
    private PrintWriter outputStream;


    private boolean connected;


    public CatBaseClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void startConnection() {
        try {
            this.socket = new Socket(host, port);
            this.socket.setReuseAddress(true);
            this.socket.setKeepAlive(true);
            this.connected = true;
            this.outputStream = new PrintWriter(socket.getOutputStream(), true);
            new Thread(new CatBaseClientHandler(this)).start();
        } catch (IOException e) {
            throw new CatBaseException(e);
        }
    }

    public void stopConnection() {
        try {
            this.socket.close();
            this.connected = false;
        } catch (IOException e) {
            throw new CatBaseException(e);
        }
    }

    public void sendMessage(@NotNull String message) {
        this.outputStream.println(message);
    }

    public Socket getSocket() {
        return socket;
    }

    public boolean isConnected() {
        return connected;
    }
}
