package cat.michal.catbase;

import cat.michal.catbase.client.CatBaseClient;
import cat.michal.catbase.client.message.MessageHandleResult;
import cat.michal.catbase.client.message.MessageHandler;
import cat.michal.catbase.common.auth.PasswordCredentials;
import cat.michal.catbase.common.message.Message;
import cat.michal.catbase.server.CatBaseServer;
import cat.michal.catbase.server.defaultImpl.DefaultQueue;
import cat.michal.catbase.server.defaultImpl.DirectExchange;
import org.junit.jupiter.api.*;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CatBaseUnsubscribeTest {
    int testingPacketId = 69;
    CatBaseServer server;
    CatBaseClient producer;
    CatBaseClient receiver;

    @BeforeAll
    void setup() throws InterruptedException {
        server = new CatBaseServer(8000);
        server.getUserManager().registerUser("prod_n", "password");
        server.getUserManager().registerUser("recv_n", "password");
        server.getExchangeManager().register(new DirectExchange("c", List.of(
                new DefaultQueue(999, "a")
        )));
        new Thread(server::startServer,"Server-Thread").start();
        Thread.sleep(600);
        producer = new CatBaseClient(new PasswordCredentials("prod_n", "password"), List.of());
        receiver = new CatBaseClient(new PasswordCredentials("recv_n", "password"), List.of());
    }


    @AfterAll
    void tearDown() {
        producer.disconnect();
        receiver.disconnect();
        server.stopServer();
    }

    @Test
    void testUnsubscribingToQueue() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            producer.connect("127.0.0.1", 8000);
            latch.countDown();
        }).start();

        latch.await();
        receiver.connect("127.0.0.1", 8000);

        receiver.subscribe("a");

        AtomicInteger received = new AtomicInteger();
        CountDownLatch messageLatch = new CountDownLatch(1);

        receiver.registerHandler(new MessageHandler<>() {

            @Override
            public MessageHandleResult handle(Message message, Object payload) {
                received.incrementAndGet();
                messageLatch.countDown();
                return MessageHandleResult.shouldRespond(false);
            }

            @Override
            public long regardingPacketId() {
                return testingPacketId;
            }
        });

        sendMessage();

        receiver.unsubscribe("a");

        sendMessage();

        messageLatch.await();

        Assertions.assertEquals(1, received.get());
    }

    void sendMessage() {
        producer.send(new Message(
                new byte[]{},
                UUID.randomUUID(),
                testingPacketId,
                "a",
                "c"
        ));
    }
}
