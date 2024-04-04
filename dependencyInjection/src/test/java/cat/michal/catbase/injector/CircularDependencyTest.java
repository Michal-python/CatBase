package cat.michal.catbase.injector;

import cat.michal.catbase.injector.exceptions.InjectorException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CircularDependencyTest {

    @Test
    void testSingleDepth() {
        boolean test = false;
        try {
            new CatBaseInjector("cat.michal.catbase.injector.singleDepth");
        } catch (InjectorException ignored) {
            test = true;
        }

        Assertions.assertTrue(test);
    }

    @Test
    void testMultipleDepth() {
        boolean test = false;
        try {
            new CatBaseInjector("cat.michal.catbase.injector.mock");
        } catch (InjectorException ignored) {
            test = true;
        }

        Assertions.assertTrue(test);
    }
}
