package cat.michal.catbase.injector.singleDepth;

import cat.michal.catbase.injector.annotations.Component;

@Component
public class SecondDependency {
    @SuppressWarnings("all")
    private final FirstDependency firstDependency;


    public SecondDependency(FirstDependency firstDependency) {
        this.firstDependency = firstDependency;
    }
}
