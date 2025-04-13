package cat.michal.catbase.injector;

import java.util.List;

/**
 * {@link cat.michal.catbase.injector.Injector} is an object on which you can perform many dependency-injection-related actions
 *
 * @author Michał
 */
public interface Injector {
    /**
     * This method calls @PreDestroy methods on all dependencies
     */
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
    List<Dependency<?>> getAll();


    /**
     * Method that registers all injectable components in given (in the constructor) package
     * In {@link CatBaseInjector} implementation, it is invoked in {@link CatBaseInjector (String)} constructor
     */
    void registerInjectables();


    /**
     * Method that returns all implementations of provided abstraction layer
     *
     * @param clazz abstraction layer type
     * @param <T> instance type
     */
    <T> List<T> getInstancesOfType(Class<T> clazz);

    /**
     * Method that returns instance of an injectable component from dependency injection container
     *
     * @param clazz class type that will be returned
     * @param dependencyName name of the dependency you want to inject
     * @return instance from dependency injection container
     * @param <T> is type that will be returned
     */
    <T> T getInstance(Class<T> clazz, String dependencyName);

    <T> T getInstance(Class<T> clazz);

    /**
     * Method that clears all previously registered injectable components
     */
    void clearInjectables();
}
