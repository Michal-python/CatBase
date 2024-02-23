import cat.michal.catbase.client.CatBaseClient;
import junit.framework.TestCase;
import org.junit.Test;

public class CatBaseClientTest extends TestCase {

        @Test
        public void testCatBaseClient() throws InterruptedException {
            CatBaseClient client = new CatBaseClient("127.0.0.1", 8080);
            client.startConnection();
            client.sendMessage("Hello1");

            Thread.sleep(30000);
            client.stopConnection();
        }

}