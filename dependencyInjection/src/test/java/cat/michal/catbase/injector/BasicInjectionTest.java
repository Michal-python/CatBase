package cat.michal.catbase.injector;

import cat.michal.catbase.injector.basicMock.ClassThatInjects;
import cat.michal.catbase.injector.basicMock.FirstDoable;
import cat.michal.catbase.injector.basicMock.NiceComponent;
import cat.michal.catbase.injector.exceptions.InjectorException;
import org.junit.jupiter.api.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BasicInjectionTest {

    private ClassThatInjects classThatInjects;
    private DefaultInjector injector;

    @BeforeAll
    void setup() {
        DefaultInjector defaultInjector = new DefaultInjector("cat.michal.catbase.injector.basicMock");
        this.injector = defaultInjector;
        this.classThatInjects = defaultInjector.getInstance(ClassThatInjects.class);
    }

    @AfterAll
    void destroy() {
        this.classThatInjects = null;
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
        Assertions.assertTrue(classThatInjects.getDoable() instanceof FirstDoable);
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
