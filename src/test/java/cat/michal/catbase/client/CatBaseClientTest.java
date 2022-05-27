package cat.michal.catbase.client;

import junit.framework.TestCase;
import org.junit.Test;

public class CatBaseClientTest extends TestCase {

        @Test
        public void testCatBaseClient() {
            CatBaseClient client = new CatBaseClient("127.0.0.1", 8080);
            new Thread(() -> {
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                client.stopConnection();
            }).start();
            client.startConnection();
            client.sendMessage("Hello1");
        }

}