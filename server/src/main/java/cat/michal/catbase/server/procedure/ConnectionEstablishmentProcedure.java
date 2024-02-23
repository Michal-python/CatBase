package cat.michal.catbase.server.procedure;

import cat.michal.catbase.common.model.CatBaseConnection;
import cat.michal.catbase.common.model.channel.Channel;

import java.net.Socket;
import java.util.UUID;

public class ConnectionEstablishmentProcedure implements Procedure<CatBaseConnection, Socket> {
    @Override
    public CatBaseConnection proceed(Socket arg) {
        return new CatBaseConnection(
                UUID.randomUUID(),
                arg,
                Channel.WAITING
        );
    }
}
