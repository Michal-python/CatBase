package cat.michal.catbase.common.model;

import java.net.Socket;
import java.util.UUID;

public class CatBaseConnection {
    private final UUID id;
    private final Socket socket;

    public CatBaseConnection(UUID id, Socket socket) {
        this.id = id;
        this.socket = socket;
    }

    public UUID getId() {
        return id;
    }

    public Socket getSocket() {
        return socket;
    }
}
