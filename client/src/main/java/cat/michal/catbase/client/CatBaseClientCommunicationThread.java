package cat.michal.catbase.client;

import java.net.Socket;

public class CatBaseClientCommunicationThread implements Runnable {
    private final Socket socket;

    public CatBaseClientCommunicationThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        //listen for messages from server
    }
}
