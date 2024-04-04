package cat.michal.catbase.server.procedure;

import cat.michal.catbase.common.model.CatBaseConnection;
import cat.michal.catbase.injector.annotations.Component;

import java.io.IOException;
import java.net.Socket;
import java.util.UUID;

@Component
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
