package cat.michal.catbase.injector;

import cat.michal.catbase.injector.provideMock.WantedDependency;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProvideTest {
    private CatBaseInjector injector;

    @BeforeAll
    void setup() {
        this.injector = new CatBaseInjector(List.of(CatBaseInjector.createExternalDependency(
                new cat.michal.catbase.injector.provideMock.TestInstance("!"),
                cat.michal.catbase.injector.provideMock.TestInstance.class
        )), "cat.michal.catbase.injector.provideMock");
    }

    @Test
    void testProvide() {
        Assertions.assertEquals("Desired Value!", this.injector.getInstance(WantedDependency.class).getField());
    }

}
