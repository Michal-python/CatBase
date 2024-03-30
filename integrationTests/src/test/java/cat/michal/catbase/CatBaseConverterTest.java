package cat.michal.catbase;

import cat.michal.catbase.client.CatBaseClient;
import cat.michal.catbase.client.message.MessageHandleResult;
import cat.michal.catbase.client.message.MessageHandler;
import cat.michal.catbase.common.auth.PasswordCredentials;
import cat.michal.catbase.common.converter.StringMessageConverter;
import cat.michal.catbase.common.message.Message;
import cat.michal.catbase.server.CatBaseServer;
import cat.michal.catbase.server.auth.UserRegistry;
import cat.michal.catbase.server.defaultImpl.DefaultQueue;
import cat.michal.catbase.server.defaultImpl.DirectExchange;
import cat.michal.catbase.server.exchange.ExchangeRegistry;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CatBaseConverterTest {
    CatBaseServer server;
    CatBaseClient client;

    @BeforeAll
    void setup() throws InterruptedException {
        server = new CatBaseServer(3020);
        UserRegistry.registerUser("user15", "password");
        ExchangeRegistry.register(new DirectExchange("exchange15", List.of(
                new DefaultQueue(200, "stringQueue")
        )));
        new Thread(server::startServer,"Server-Thread").start();
        Thread.sleep(600);
        client = new CatBaseClient(new PasswordCredentials("user15", "password"), List.of(), new StringMessageConverter());
    }


    @AfterAll
    void tearDown() {
        client.disconnect();
        server.stopServer();
    }

    @Test
    void testSimpleConnection() throws InterruptedException {
        client.connect("127.0.0.1", 3020);
        try {
            Thread.sleep(1500);
        } catch (InterruptedException ignored) {
        }

        AtomicReference<String> receivedMessage = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        client.registerHandler(new MessageHandler<String>() {
            @Override
            public MessageHandleResult handle(Message msg, String message) {
                receivedMessage.set(message);
                latch.countDown();
                return MessageHandleResult.shouldRespond(false);
            }

            @Override
            public String queue() {
                return "stringQueue";
            }
        });

        client.subscribe("stringQueue");
        client.convertAndSend("Hello World!!!", "exchange15", "stringQueue");

        latch.await();
        Assertions.assertEquals("Hello World!!!", receivedMessage.get());
    }
}
