package cat.michal.catbase.injector.provideMock;

public class WantedDependency {
    private final String field;

    public WantedDependency(String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
