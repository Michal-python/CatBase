package cat.michal.catbase.injector.singleDepth;

import cat.michal.catbase.injector.annotations.Component;

@Component
public class FirstDependency {
    @SuppressWarnings("all")
    private final SecondDependency secondDependency;

    public FirstDependency(SecondDependency secondDependency) {
        this.secondDependency = secondDependency;
    }
}
