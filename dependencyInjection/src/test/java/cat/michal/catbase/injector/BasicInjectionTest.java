package cat.michal.catbase.injector;

import cat.michal.catbase.injector.basicMock.ClassThatInjects;
import cat.michal.catbase.injector.basicMock.FirstDoable;
import cat.michal.catbase.injector.basicMock.NiceComponent;
import cat.michal.catbase.injector.exceptions.InjectorException;
import org.junit.jupiter.api.*;

public class BasicInjectionTest {

    static ClassThatInjects classThatInjects;
    static CatBaseInjector injector;

    @BeforeAll
    static void setup() {
        CatBaseInjector catBaseInjector = new CatBaseInjector("cat.michal.catbase.injector.basicMock");
        injector = catBaseInjector;
        classThatInjects = catBaseInjector.getInstance(ClassThatInjects.class);
    }

    @AfterAll
    static void destroy() {
        classThatInjects = null;
    }

    @Test
    void testBasicInjection() {
        Assertions.assertNotNull(classThatInjects);
    }

    @Test
    void testConstructorInjection() {
        Assertions.assertNotNull(classThatInjects.getDoable());
    }

    @Test
    void testConstructorInstantiationInjection() {
        Assertions.assertInstanceOf(FirstDoable.class, classThatInjects.getDoable());
    }

    @Test
    void testResult() {
        Assertions.assertEquals(1, classThatInjects.testResult());
    }

    @Test
    void testFieldInjection() {
        Assertions.assertNotNull(classThatInjects.getInjectedClass());
    }

    @Test
    void testExclude() {
        boolean pass = false;

        try {
            injector.getInstance(NiceComponent.class);
        } catch (InjectorException ignored) {
            pass = true;
        }

        Assertions.assertTrue(pass);
    }
    @Test
    void testFieldInstantiationInjection() {
        Assertions.assertEquals("Secret value", classThatInjects.getInjectedClass().getValue());
    }
}
