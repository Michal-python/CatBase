package cat.michal.catbase.injector;

import cat.michal.catbase.injector.exceptions.InjectorException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CircularDependencyTest {

    @Test
    void testSingleDepth() {
        Assertions.assertThrows(InjectorException.class, () -> new CatBaseInjector("cat.michal.catbase.injector.singleDepth"));
    }

    @Test
    void testMultipleDepth() {
        Assertions.assertThrows(InjectorException.class, () -> new CatBaseInjector("cat.michal.catbase.injector.mock"));
    }
}
