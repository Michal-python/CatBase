package cat.michal.catbase.injector;

import cat.michal.catbase.injector.listInjection.FirstImpl;
import cat.michal.catbase.injector.listInjection.InjectTo;
import cat.michal.catbase.injector.listInjection.SecondImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ListInjectionTest {
    private CatBaseInjector injector;

    @BeforeAll
    void setup() {
        this.injector = new CatBaseInjector("cat.michal.catbase.injector.listInjection");
    }

    @Test
    void testListInjection() {
        InjectTo instance = this.injector.getInstance(InjectTo.class);

        Assertions.assertEquals(3, instance.getLayers().size());
        Assertions.assertInstanceOf(FirstImpl.class, instance.getFirstLayer());
        Assertions.assertInstanceOf(SecondImpl.class, instance.getSecondLayer());
    }

}
