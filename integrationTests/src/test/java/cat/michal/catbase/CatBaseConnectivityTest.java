package cat.michal.catbase;

import cat.michal.catbase.client.CatBaseClient;
import cat.michal.catbase.client.CatBaseClientBuilder;
import cat.michal.catbase.common.auth.PasswordCredentials;
import cat.michal.catbase.server.CatBaseServer;
import org.junit.jupiter.api.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CatBaseConnectivityTest {
    CatBaseServer server;
    CatBaseClient client;

    @BeforeAll
    void setup() throws InterruptedException, UnknownHostException {
        server = new CatBaseServer(8000);
        server.getUserManager().registerUser("user", "password");
        new Thread(server::startServer,"Server-Thread").start();
        Thread.sleep(600);
        client = CatBaseClientBuilder.newBuilder()
                .credentials(new PasswordCredentials("user", "password"))
                .address(InetAddress.getLocalHost())
                .port(8000)
                .build();
    }

    @AfterAll
    void tearDown() {
        client.disconnect();
        server.stopServer();
    }

    @Test
    void testSimpleConnection() {
        client.connect();
        try {
            Thread.sleep(1500);
        } catch (InterruptedException ignored) {
        }
        Assertions.assertTrue(client.isConnected());
    }
}
