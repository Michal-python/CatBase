package cat.michal.catbase.injector.mock;

import cat.michal.catbase.injector.annotations.Component;

@Component
public class SecondHolder {
    @SuppressWarnings("all")
    private final FourthDependency fourthDependency;

    public SecondHolder(FourthDependency fourthDependency) {
        this.fourthDependency = fourthDependency;
    }
}
