package cat.michal.catbase.common.model;

import cat.michal.catbase.common.model.channel.Channel;

import java.net.Socket;
import java.util.UUID;

public class CatBaseConnection {
    private final UUID id;
    private final Socket socket;
    private Channel connectedChannel;

    public CatBaseConnection(UUID id, Socket socket, Channel connectedChannel) {
        this.id = id;
        this.socket = socket;
        this.connectedChannel = connectedChannel;
    }

    public UUID getId() {
        return id;
    }

    public Socket getSocket() {
        return socket;
    }

    public Channel getConnectedChannel() {
        return connectedChannel;
    }

    public void setConnectedChannel(Channel connectedChannel) {
        this.connectedChannel = connectedChannel;
    }
}
