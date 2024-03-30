import cat.michal.catbase.common.message.Message;
import cat.michal.catbase.common.model.CatBaseConnection;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MessageExchangeTest {

    @Test
    void pureDataTest() throws IOException {
        CBORMapper cbor = new CBORMapper();
        cbor.getFactory().disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        Message message1 = new Message("Hello".getBytes(), UUID.randomUUID(), 0, "a", "b");
        Message message2 = new Message("World".getBytes(), UUID.randomUUID(), 1, "c", "d");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        cbor.writer().writeValue(outputStream, message1);
        cbor.writer().writeValue(outputStream, message2);


        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        CBORFactory cborFactory = new CBORFactory();
        ObjectMapper mapper = new ObjectMapper(cborFactory);

        mapper.getFactory().disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);

        JsonParser parser = cborFactory.createParser(inputStream);
        Message message3 = mapper.readValue(parser, Message.class);
        Message message4 = mapper.readValue(parser, Message.class);
        assertEquals(message1, message3);
        assertEquals(message2, message4);
    }

    private static ServerSocket serverSocket = null;
    private static Socket clientSocket = null;
    private static CatBaseConnection serverConn;
    private static CatBaseConnection clientConn;
    private static final CountDownLatch countDownLatch = new CountDownLatch(1);

    @BeforeAll
    static void setup() throws IOException, InterruptedException {
        serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress("localhost", 0));
        int port = serverSocket.getLocalPort();
        Thread serverThread = new Thread(() -> {
            try {
                countDownLatch.countDown();
                Socket serverSocketConnection = serverSocket.accept();
                serverConn = new CatBaseConnection(UUID.randomUUID(), serverSocketConnection);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        serverThread.start();

        countDownLatch.await();
        clientSocket = new Socket("localhost", port);
        clientSocket.setKeepAlive(true);
        clientSocket.setReuseAddress(true);
        clientConn = new CatBaseConnection(UUID.randomUUID(), clientSocket);
    }

    @AfterAll
    static void teardown() throws IOException {
        serverSocket.close();
        clientSocket.close();
        clientConn.close();
        serverConn.close();
    }

    @Test
    public void testTransportProtocol() throws IOException, InterruptedException {
        Message message = new Message("Goodbye World!".getBytes(), UUID.randomUUID(), 0, "key", "queue");
        message.getHeaders().put("Header1", "Hello");
        message.getHeaders().put("Header2", 10);

        CountDownLatch countDownLatch = new CountDownLatch(1);
        new Thread(() -> {
            try {
                countDownLatch.countDown();
                clientConn.readMessage();
            } catch (IOException ignored) {}
        }).start();

        countDownLatch.await();
        clientConn.sendPacket(message);
        Message received = serverConn.readMessage();

        assertEquals(message, received);
        assertEquals("Goodbye World!", new String(received.getPayload()));
    }

}
