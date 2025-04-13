package cat.michal.catbase.injector;

import cat.michal.catbase.injector.exceptions.InjectorException;

import java.util.*;
import java.util.stream.Collectors;

public class CatBaseInjector implements Injector {
    private final InjectionContext injectionContext;
    private final InjectionExecutor injectionExecutor;
    private final InjectorInstanceFactory instanceFactory;

    public CatBaseInjector(String packagePath) {
        this(List.of(packagePath), null);
    }

    public CatBaseInjector(String packagePath, ClassLoader classLoader) {
        this(List.of(packagePath), classLoader);
    }

    public CatBaseInjector(Collection<String> packagePaths) {
        this(packagePaths, CatBaseInjector.class.getClassLoader());
    }

    public CatBaseInjector(Collection<String> packagePaths, ClassLoader classLoader) {
        this.injectionContext = new InjectionContext(packagePaths, classLoader);
        this.injectionExecutor = new InjectionExecutor(this.injectionContext, this);
        this.instanceFactory = new InjectorInstanceFactory(this, this.injectionContext);

        this.registerInjectables();
    }

    @Override
    public void destroy() {
        this.injectionContext.getDependencies().forEach(injectionExecutor::invokePreDestroy);

        this.clearInjectables();
    }


    @Override
    public <T> T createInstance(Class<T> clazz) {
        return this.instanceFactory.createInstance(clazz);
    }

    @Override
    public List<Dependency<?>> getAll() {
        return Collections.unmodifiableList(this.injectionContext.getDependencies());
    }

    @Override
    public void registerInjectables() {
        this.injectionContext.createDependencyTree();

        // check for nested dependencies for provided instances
        // this step is necessary because the provide method dependency needs to depend on containing class due to lack of its instance when calling this method
        this.injectionContext.getDependencies().stream().filter(dependency -> dependency.getProvideMethod() != null).forEach(dependency -> {
           Dependency<?> containingDependency = this.injectionContext.getDependency(dependency.getProvideMethod().getDeclaringClass())
                    .orElseThrow(() -> new InjectorException("Dependency (associated with provide method '" + dependency.getProvideMethod().getName() + "') " + dependency.getProvideMethod().getDeclaringClass().getName() + " not found"));

           dependency.addDependency(containingDependency);
        });

        //check for nested dependencies
        this.injectionContext.getDependencies().stream()
                // filter only dependencies that are needed for the dependency tree
                .filter(dependency -> dependency.getProvideMethod() == null && dependency.getInstance() == null)
                .forEach(dependency -> {
                    this.injectionContext.findDependants(dependency).forEach(dependency::addDependency);
                });

        //check for circular dependencies
        Set<Dependency<?>> visited = new HashSet<>();
        for (Dependency<?> dependency : this.injectionContext.getDependencies()) {
            if (!visited.contains(dependency)) {
                checkCircularDependency(dependency, visited, new HashSet<>());
            }
        }

        //initialize dependencies
        List<Dependency<?>> sortedDependencies = new ArrayList<>();
        this.injectionContext.getDependencies().forEach(dependency -> initializeDependency(dependency, sortedDependencies));

        //all dependencies initialized at this point
        // creating instances and injecting proper dependency instances
        sortedDependencies.stream()
                .filter(dependency -> dependency.getInstance() == null)
                .forEach(dependency -> {
            dependency.setInstance(this.instanceFactory.createInstance(dependency));
            this.injectionExecutor.injectField(dependency.getInstance());

            this.injectionExecutor.invokePostConstruct(dependency);
        });
    }

    private void initializeDependency(Dependency<?> dependency, List<Dependency<?>> sortedDependencies) {
        if (!sortedDependencies.contains(dependency)) {
            dependency.getDepends().forEach(dep -> initializeDependency(dep, sortedDependencies));
            sortedDependencies.add(dependency);
        }
    }

    private void checkCircularDependency(Dependency<?> dependency, Set<Dependency<?>> visited, Set<Dependency<?>> stack) {
        if (stack.contains(dependency)) {
            throw new InjectorException("Circular dependency detected involving " + stack.stream().map(clazz -> clazz.getClass().getSimpleName()).collect(Collectors.joining()));
        }
        if (!visited.contains(dependency)) {
            visited.add(dependency);
            stack.add(dependency);
            for (Dependency<?> dependent : dependency.getDepends()) {
                checkCircularDependency(dependent, visited, stack);
            }
            stack.remove(dependency);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> getInstancesOfType(Class<T> clazz) {
        return (List<T>) this.injectionContext.getDependencies().stream()
                .filter(dependency -> clazz.isAssignableFrom(dependency.getClazz()))
                .map(Dependency::getInstance)
                .toList();
    }

    @Override
    public <T> T getInstance(Class<T> clazz, String dependencyName) {
        return this.injectionContext.findBestMatchingDependency(clazz, dependencyName)
                .orElseThrow(() -> new InjectorException("Could not find dependency of type " + clazz.getName()))
                .getInstance();
    }

    @Override
    public <T> T getInstance(Class<T> clazz) {
        return this.getInstance(clazz, null);
    }

    @Override
    public void clearInjectables() {
        this.injectionContext.getDependencies().clear();
    }

    InjectionExecutor getInjectionExecutor() {
        return injectionExecutor;
    }
}
