package cat.michal.catbase.injector;

import cat.michal.catbase.injector.provideMock.WantedDependency;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProvideTest {
    private CatBaseInjector injector;

    @BeforeAll
    void setup() {
        this.injector = new CatBaseInjector("cat.michal.catbase.injector.provideMock");
    }

    @Test
    void testProvide() {
        Assertions.assertEquals("Desired Value!", this.injector.getInstance(WantedDependency.class).getField());
    }

}
