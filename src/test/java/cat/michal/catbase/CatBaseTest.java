package cat.michal.catbase;

import org.junit.Test;

class CatBaseTest {

        @Test
        public void testServer() {
            CatBase catBase = new CatBase(8080);
            catBase.startServer();
        }
}