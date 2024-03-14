import cat.michal.catbase.client.CatBaseClient;
import org.junit.jupiter.api.Test;

public class CatBaseClientTest {

        @Test
        public void testCatBaseClient() throws InterruptedException {
            CatBaseClient client = new CatBaseClient("127.0.0.1", 8080);
            client.startConnection();
            client.sendMessage("Hello1");

            Thread.sleep(30000);
            client.stopConnection();
        }

}