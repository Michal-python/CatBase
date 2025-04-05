package cat.michal.catbase.injector.mixedTest;

import cat.michal.catbase.injector.annotations.Component;

@Component
public class MapperDependant {
    private final Mapper mapper;
    public MapperDependant(Mapper mapper) {
        this.mapper = mapper;
    }

    public String invoke() {
        return mapper.map("value");
    }
}
