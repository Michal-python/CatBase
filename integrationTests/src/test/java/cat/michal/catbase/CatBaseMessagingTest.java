package cat.michal.catbase;

import cat.michal.catbase.client.CatBaseClient;
import cat.michal.catbase.client.CatBaseClientBuilder;
import cat.michal.catbase.client.message.MessageHandleResult;
import cat.michal.catbase.client.message.MessageHandler;
import cat.michal.catbase.common.auth.PasswordCredentials;
import cat.michal.catbase.common.message.Message;
import cat.michal.catbase.server.CatBaseServer;
import cat.michal.catbase.server.defaultImpl.DefaultQueue;
import cat.michal.catbase.server.defaultImpl.DirectExchange;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class CatBaseMessagingTest {
    static CatBaseServer server;
    static CatBaseClient producer;
    static CatBaseClient receiver;

    @BeforeAll
    static void setup() throws InterruptedException, UnknownHostException {
        server = new CatBaseServer(6969);
        server.getUserManager().registerUser("prod", "password");
        server.getUserManager().registerUser("recv", "password");
        server.getExchangeManager().register(new DirectExchange("b", List.of(
                new DefaultQueue(999, "ab")
        )));
        new Thread(server::startServer,"Server-Thread").start();
        Thread.sleep(600);
        producer = CatBaseClientBuilder.newBuilder()
                .address(InetAddress.getLocalHost())
                .port(6969)
                .credentials(new PasswordCredentials("prod", "password"))
                .build();
        receiver = CatBaseClientBuilder.newBuilder()
                .address(InetAddress.getLocalHost())
                .port(6969)
                .credentials(new PasswordCredentials("recv", "password"))
                .build();
    }


    @AfterAll
    static void tearDown() {
        producer.disconnect();
        receiver.disconnect();
        server.stopServer();
    }

    @Test
    void testSimpleMessage() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            producer.connect();
            latch.countDown();
        }).start();

        latch.await();
        receiver.connect();

        receiver.subscribe("ab");

        AtomicBoolean passed = new AtomicBoolean();

        receiver.registerHandler(new MessageHandler<>() {
            @Override
            public MessageHandleResult handle(Message message, Object payload) {
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
                "ab",
                "b"
        ));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }

        Assertions.assertTrue(passed.get());
    }
}
