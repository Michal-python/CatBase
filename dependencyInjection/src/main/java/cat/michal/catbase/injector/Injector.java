package cat.michal.catbase.injector;

import java.util.List;

/**
 * {@link cat.michal.catbase.injector.Injector} is an object on which you can perform many dependency-injection-related actions
 *
 * @author Michał
 */
public interface Injector {

    void destroy();

    /**
     * Method for creating new instances of the provided class
     *
     * @param clazz class type that will be returned
     * @return newly created instance
     * @param <T> is type which instance will be created
     */

    <T> T createInstance(Class<T> clazz);

    /**
     * Method for getting all registered injectable components
     *
     * @return List of {@link Dependency}
     */
    @SuppressWarnings("unused")
    List<Dependency<?>> getAll();


    /**
     * Method that registers all injectable components in given (in the constructor) package
     * In {@link CatBaseInjector} implementation, it is invoked in {@link CatBaseInjector (String)} constructor
     */
    void registerInjectables();

    /**
     * Method that injects registered injectable components to fields of the given instance
     *
     * @param instance class which fields will be injected
     * @param <T> instance type
     */
    <T> void injectField(T instance);

    /**
     * Method that returns instance of an injectable component from dependency injection container
     *
     * @param clazz class type that will be returned
     * @return instance from dependency injection container
     * @param <T> is type that will be returned
     */
    <T> T getInstance(Class<T> clazz);

    /**
     * Method that clears all previously registered injectable components
     */
    @SuppressWarnings("unused")
    void clearInjectables();
}
