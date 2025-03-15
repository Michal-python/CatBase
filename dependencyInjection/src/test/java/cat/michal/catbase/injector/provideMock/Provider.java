package cat.michal.catbase.injector.provideMock;

import cat.michal.catbase.injector.annotations.Component;
import cat.michal.catbase.injector.annotations.Provide;

@Component
public class Provider {
    private final TestInstance instance;

    public Provider(TestInstance instance) {
        this.instance = instance;
    }

    @Provide
    public WantedDependency provide(NeededDependency depend) {
        return new WantedDependency(depend.field + " Value" + instance.name);
    }
}
