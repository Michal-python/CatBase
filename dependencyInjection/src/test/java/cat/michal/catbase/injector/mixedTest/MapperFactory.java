package cat.michal.catbase.injector.mixedTest;

import cat.michal.catbase.injector.annotations.Component;
import cat.michal.catbase.injector.annotations.Provide;

import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class MapperFactory {
    private Logger logger;

    public MapperFactory(Logger logger) {
        this.logger = logger;
    }

    @Provide
    @SuppressWarnings("unused")
    public Mapper createMapper(OuterDependency outerDependency) {
        return value -> {
            logger.log(Level.INFO, "test");
            return outerDependency.consume(value);
        };
    }
}
