package cat.michal.catbase.injector.mock;

import cat.michal.catbase.injector.annotations.Component;

@Component
public class ThirdDependency {
    @SuppressWarnings("all")
    private final SecondHolder secondHolder;

    public ThirdDependency(SecondHolder secondHolder) {
        this.secondHolder = secondHolder;
    }
}
