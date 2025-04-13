package cat.michal.catbase.injector.mixedTest;

import cat.michal.catbase.injector.annotations.Component;
import cat.michal.catbase.injector.annotations.ExternalDependency;

import java.util.logging.Logger;

@Component
public class MapperDependant {
    @ExternalDependency
    @SuppressWarnings("unused")
    public static final OuterDependency asd = String::toUpperCase;
    @ExternalDependency
    @SuppressWarnings("unused")
    public static final Logger LOGGER = Logger.getLogger("Some name");

    private final Mapper mapper;
    public MapperDependant(Mapper mapper) {
        this.mapper = mapper;
    }

    public String invoke() {
        return mapper.map("value");
    }
}
