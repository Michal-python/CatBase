package cat.michal.catbase.injector.provideMock;

import cat.michal.catbase.injector.annotations.Component;
import cat.michal.catbase.injector.annotations.Provide;

@Component
public class Provider {

    @Provide
    public WantedDependency provide(NeededDependency depend) {
        return new WantedDependency(depend.field + " Value!");
    }
}
