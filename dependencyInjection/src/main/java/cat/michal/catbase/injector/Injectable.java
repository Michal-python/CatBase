package cat.michal.catbase.injector;

/**
 *
 * Class that holds information about class that can be injected
 * It also has `name` field which is used for identifying injectable fields
 *
 * @param <T> The type of the injectable
 * @author Michał
 *
 */
public class Injectable<T> {

    private final String name;
    private final Class<T> clazz;
    private final T instance;

    public Injectable(String name, Class<T> clazz, T instance) {
        this.name = name;
        this.clazz = clazz;
        this.instance = instance;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public T getInstance() {
        return instance;
    }

    public String getName() {
        return name;
    }
}