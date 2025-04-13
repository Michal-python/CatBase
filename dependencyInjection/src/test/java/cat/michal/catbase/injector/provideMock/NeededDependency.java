package cat.michal.catbase.injector.provideMock;

import cat.michal.catbase.injector.annotations.Component;
import cat.michal.catbase.injector.annotations.ExternalDependency;

import java.util.logging.Logger;

@Component
public class NeededDependency {
    public String field = "Desired";

    @ExternalDependency
    public static final TestInstance TEST_INSTANCE = new TestInstance("!");
    @ExternalDependency
    public static final Logger LOGGER = Logger.getLogger("Logger");
}
