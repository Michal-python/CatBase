package cat.michal.catbase.injector;

import cat.michal.catbase.injector.multipleInstances.InstanceHolder;
import cat.michal.catbase.injector.multipleInstances.SecondInstance;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MultipleInstanceTest {

    @Test
    void testMultipleInstances() {
        CatBaseInjector injector = new CatBaseInjector(List.of("cat.michal.catbase.injector.multipleInstances", "cat.michal.catbase.injector.otherPackage"));

        Assertions.assertEquals(4, injector.getInstancesOfType(InstanceHolder.class).size());
        Assertions.assertTrue(injector.getInstance(InstanceHolder.class) instanceof SecondInstance);
    }
}
