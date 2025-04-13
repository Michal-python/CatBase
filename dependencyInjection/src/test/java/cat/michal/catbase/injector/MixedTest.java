package cat.michal.catbase.injector;

import cat.michal.catbase.injector.mixedTest.MapperDependant;
import cat.michal.catbase.injector.mixedTest.OuterDependency;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;
import java.util.logging.Logger;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MixedTest {
    private CatBaseInjector injector;

    @BeforeAll
    void setup() {
        this.injector = new CatBaseInjector("cat.michal.catbase.injector.mixedTest");
    }

    @Test
    void testMixed() {
        Assertions.assertEquals("VALUE", this.injector.getInstance(MapperDependant.class).invoke());
    }

}
