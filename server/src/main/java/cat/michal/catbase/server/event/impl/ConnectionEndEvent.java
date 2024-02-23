package cat.michal.catbase.server.event.impl;

import cat.michal.catbase.common.model.CatBaseConnection;
import cat.michal.catbase.server.event.Event;

public class ConnectionEndEvent implements Event {
    private final CatBaseConnection connection;

    public ConnectionEndEvent(CatBaseConnection connection) {
        this.connection = connection;
    }

    public CatBaseConnection getConnection() {
        return connection;
    }
}
