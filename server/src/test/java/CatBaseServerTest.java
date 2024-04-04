import cat.michal.catbase.server.CatBaseServer;
import cat.michal.catbase.server.defaultImpl.DefaultQueue;
import cat.michal.catbase.server.defaultImpl.DirectExchange;
import org.junit.jupiter.api.Test;

import java.util.List;

public class CatBaseServerTest {

    @Test
    public void testServer() {
        CatBaseServer catBase = new CatBaseServer(8080);

        catBase.getExchangeManager().register(new DirectExchange("exchange", List.of(new DefaultQueue(200, "queue"))));

        new Thread(() -> {
            try {
                Thread.sleep(500);
                catBase.stopServer();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
        catBase.startServer();
    }
}