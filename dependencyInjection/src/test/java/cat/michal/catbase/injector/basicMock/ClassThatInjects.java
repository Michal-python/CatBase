package cat.michal.catbase.injector.basicMock;

import cat.michal.catbase.injector.annotations.Component;
import cat.michal.catbase.injector.annotations.Inject;

@Component
public class ClassThatInjects {
    private final Doable doable;
    @Inject
    @SuppressWarnings("unused")
    private InjectedClass injectedClass;

    public ClassThatInjects(Doable doable) {
        this.doable = doable;
    }

    public int testResult() {
        return doable.result();
    }

    public InjectedClass getInjectedClass() {
        return injectedClass;
    }

    public Doable getDoable() {
        return doable;
    }
}
