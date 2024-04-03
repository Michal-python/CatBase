package cat.michal.catbase.injector.basicMock;

import cat.michal.catbase.injector.annotations.Component;

@Component
public class InjectedClass {
    private final String value;

    public InjectedClass() {
        this.value = "Secret value";
    }

    public String getValue() {
        return value;
    }
}
