package cat.michal.catbase.injector.mock;

import cat.michal.catbase.injector.annotations.Component;

@Component
public class FourthDependency {
    @SuppressWarnings("all")
    private final ThirdDependency thirdDependency;

    public FourthDependency(ThirdDependency thirdDependency) {
        this.thirdDependency = thirdDependency;
    }
}
