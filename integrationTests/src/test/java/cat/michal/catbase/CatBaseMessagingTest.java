package cat.michal.catbase;

import cat.michal.catbase.client.CatBaseClient;
import cat.michal.catbase.client.message.MessageHandleResult;
import cat.michal.catbase.client.message.MessageHandler;
import cat.michal.catbase.common.auth.PasswordCredentials;
import cat.michal.catbase.common.message.Message;
import cat.michal.catbase.server.CatBaseServer;
import cat.michal.catbase.server.auth.UserRegistry;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CatBaseMessagingTest {
    CatBaseServer server;
    CatBaseClient producer;
    CatBaseClient receiver;

    @BeforeAll
    void setup() throws InterruptedException {
        server = new CatBaseServer(8000);
        UserRegistry.registerUser("prod", "password");
        UserRegistry.registerUser("recv", "password");
        new Thread(server::startServer,"Server-Thread").start();
        Thread.sleep(600);
        producer = new CatBaseClient(new PasswordCredentials("prod", "password"), List.of());
        receiver = new CatBaseClient(new PasswordCredentials("recv", "password"), List.of());
    }


    @AfterAll
    void tearDown() {
        producer.disconnect();
        receiver.disconnect();
        server.stopServer();
    }

    @Test
    void testSimpleMessage() {
        producer.connect("127.0.0.1", 8000);
        receiver.connect("127.0.0.1", 8000);

        AtomicBoolean passed = new AtomicBoolean();

        receiver.registerHandler(new MessageHandler() {
            @Override
            public MessageHandleResult handle(Message message) {
                passed.set(message.getPayload()[0] == 101);
                return null;
            }

            @Override
            public long regardingPacketId() {
                return 30;
            }
        });

        producer.send(new Message(
                new byte[]{101},
                UUID.randomUUID(),
                30,
                "a",
                "b"
        ));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }

        Assertions.assertTrue(passed.get());
    }
}
