package cat.michal.catbase.server.procedure;

import cat.michal.catbase.common.model.CatBaseConnection;

import java.io.IOException;
import java.net.Socket;
import java.util.UUID;

public class ConnectionEstablishmentProcedure implements Procedure<CatBaseConnection, Socket> {
    @Override
    public CatBaseConnection proceed(Socket arg) {
        try {
            return new CatBaseConnection(UUID.randomUUID(), arg);
        } catch (IOException e) {
            return null;
        }
    }
}
