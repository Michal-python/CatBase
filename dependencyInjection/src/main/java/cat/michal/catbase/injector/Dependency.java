package cat.michal.catbase.injector;

import java.util.ArrayList;
import java.util.List;

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
    private final List<Dependency<?>> depends;
    private T instance;

    public Dependency(String name, Class<T> clazz, T instance) {
        this.name = name;
        this.clazz = clazz;
        this.instance = instance;
        this.depends = new ArrayList<>();
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public <D> void addDependency(Dependency<D> dependency) {
        this.depends.add(dependency);
    }

    public List<Dependency<?>> getDepends() {
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
}