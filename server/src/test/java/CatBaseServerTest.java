import cat.michal.catbase.server.CatBaseServer;
import org.junit.Test;

public class CatBaseServerTest {

    @Test
    public void testServer() {
        CatBaseServer catBase = new CatBaseServer(8080);
        new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            catBase.stopServer();
        }).start();
        catBase.startServer();
    }
}