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
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

public class CatBaseSendAndReceiveTest {

    static CatBaseServer server;
    static CatBaseClient producer;
    static CatBaseClient receiver;

    @BeforeAll
    static void setup() throws InterruptedException, UnknownHostException {
        server = new CatBaseServer(3273);
        server.getUserManager().registerUser("prod_2243", "password");
        server.getUserManager().registerUser("recv_2243", "password");
        server.getExchangeManager().register(new DirectExchange("b_2243", List.of(
                new DefaultQueue(999, "ab_2243")
        )));
        new Thread(server::startServer,"Server-Thread").start();
        Thread.sleep(600);
        producer = CatBaseClientBuilder.newBuilder()
                .address(InetAddress.getLocalHost())
                .port(3273)
                .credentials(new PasswordCredentials("prod_2243", "password"))
                .build();
        receiver = CatBaseClientBuilder.newBuilder()
                .address(InetAddress.getLocalHost())
                .port(3273)
                .credentials(new PasswordCredentials("recv_2243", "password"))
                .build();
    }


    @AfterAll
    static void tearDown() {
        producer.disconnect();
        receiver.disconnect();
        server.stopServer();
    }

    @Test
    void testSimpleMessage() throws InterruptedException, ExecutionException {
        CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            producer.connect();
            latch.countDown();
        }).start();

        latch.await();
        receiver.connect();

        receiver.subscribe("ab_2243");

        receiver.registerHandler(new MessageHandler<>() {
            @Override
            public MessageHandleResult handle(Message message, Object payload) {

                return MessageHandleResult.response(new Message("Hello".getBytes()).withHeaders(message.getHeaders()));
            }
        });

        var response = producer.sendAndReceive(new Message(
                new byte[]{101},
                "ab_2243",
                "b_2243"
        ).withHeaders(Map.of("Header1", 2)));

        Assertions.assertArrayEquals("Hello".getBytes(), response.get().getPayload());
        Assertions.assertEquals(2, response.get().getHeaders().get("Header1"));
    }

}
