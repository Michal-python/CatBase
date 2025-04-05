package cat.michal.catbase.injector.mixedTest;

@FunctionalInterface
public interface OuterDependency {
    String consume(String value);
}
