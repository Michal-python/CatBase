package cat.michal.catbase;

import cat.michal.catbase.client.CatBaseClient;
import cat.michal.catbase.common.auth.PasswordCredentials;
import cat.michal.catbase.server.CatBaseServer;
import org.junit.jupiter.api.*;

import java.util.List;
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CatBaseConnectivityTest {
    CatBaseServer server;
    CatBaseClient client;

    @BeforeAll
    void setup() throws InterruptedException {
        server = new CatBaseServer(8000);
        server.getUserManager().registerUser("user", "password");
        new Thread(server::startServer,"Server-Thread").start();
        Thread.sleep(600);
        client = new CatBaseClient(new PasswordCredentials("user", "password"), List.of());
    }


    @AfterAll
    void tearDown() {
        client.disconnect();
        server.stopServer();
    }

    @Test
    void testSimpleConnection() {
        client.connect("127.0.0.1", 8000);
        try {
            Thread.sleep(1500);
        } catch (InterruptedException ignored) {
        }
        Assertions.assertTrue(client.isConnected());
    }
}
