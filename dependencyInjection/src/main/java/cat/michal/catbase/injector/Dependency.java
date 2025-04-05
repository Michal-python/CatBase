package cat.michal.catbase.injector;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * Class that holds information about class that can be injected
 * It also has `name` field which is used for identifying injectable fields
 *
 * @param <T> The type of the injectable
 * @author Michał
 *
 */
public class Dependency<T> {

    private final String name;
    private final Class<T> clazz;
    private final Set<Dependency<?>> depends;
    private T instance;
    private final Method provideMethod;
    private final Class<?> containingClass;

    public Dependency(String name, Class<T> clazz, T instance, Method provideMethod, Class<?> containingClass) {
        this.name = name;
        this.clazz = clazz;
        this.instance = instance;
        this.provideMethod = provideMethod;
        this.depends = new HashSet<>();
        this.containingClass = containingClass;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public Method getProvideMethod() {
        return provideMethod;
    }

    public Class<?> getContainingClass() {
        return containingClass;
    }

    public <D> void addDependency(Dependency<D> dependency) {
        this.depends.add(dependency);
    }

    public Set<Dependency<?>> getDepends() {
        return depends;
    }

    public T getInstance() {
        return instance;
    }

    public String getName() {
        return name;
    }

    @SuppressWarnings("unchecked")
    public void setInstance(Object instance) {
        this.instance = (T) instance;
    }

    @Override
    public String toString() {
        return "Dependency{" +
                "class=" + clazz.getSimpleName() +
                ", name='" + name + '\'' +
                '}';
    }
}
