package cat.michal.catbase.injector;

import cat.michal.catbase.injector.postConstruct.PostConstructObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PostConstructTest {
    private DefaultInjector injector;

    @BeforeAll
    void setup() {
        this.injector = new DefaultInjector("cat.michal.catbase.injector.postConstruct");
    }

    @Test
    void testPostConstructInvocation() {
        Assertions.assertEquals("Value!", this.injector.getInstance(PostConstructObject.class).getField());
    }

}
