package cat.michal.catbase.injector;

import cat.michal.catbase.injector.annotations.*;
import cat.michal.catbase.injector.exceptions.InjectorException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.*;

public class CatBaseInjector implements Injector {
    private final List<Dependency<?>> dependencies;
    private final List<Class<?>> classes;
    public CatBaseInjector(String packagePath) {
        this(new ArrayList<>(), packagePath);
    }

    private CatBaseInjector(List<Dependency<?>> dependencies, String packagePath) {
        this.dependencies = dependencies;
        this.classes = ClassFinder.findAllClasses(packagePath).stream().toList();

        this.registerInjectables();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T createInstance(Class<T> clazz) {
        if(clazz.isEnum()) {
            throw new InjectorException("Enums are not allowed");
        }
        if(clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
            List<? extends Class<? extends T>> implementations = classes.stream()
                    .filter(clazz::isAssignableFrom)
                    .map(classElement -> (Class<? extends T>) classElement)
                    .toList();
            if(implementations.isEmpty()) {
                throw new InjectorException("No implementation found for abstraction layer " + clazz.getName());
            }

            //if only one implementation - no need to look for primary implementations
            if(implementations.size() == 1) {
                return createInstance(implementations.get(0));
            }
            List<? extends Class<? extends T>> primaryImplementations = implementations.stream()
                    .filter(injectable -> injectable.isAnnotationPresent(Primary.class))
                    .toList();
            if(primaryImplementations.size() > 1) {
                throw new InjectorException("More than one primary implementation of abstraction layer " + clazz.getName());
            }
            if(primaryImplementations.isEmpty()) {
                throw new InjectorException("No primary implementation found for abstraction layer " + clazz.getName());
            }
            return createInstance(primaryImplementations.get(0));
        }

        return instantiate((Dependency<? extends T>) dependencies.stream()
                .filter(dependency -> dependency.getClazz().equals(clazz))
                .findAny()
                .orElseThrow(() -> new InjectorException("Dependency for class " + clazz.getName() + " not found. Consider adding @Component annotation"))
        );
    }

    @Override
    public List<Dependency<?>> getAll() {
        return Collections.unmodifiableList(dependencies);
    }

    @Override
    public void registerInjectables() {
        classes.stream()
                .filter(clazz -> clazz.isAnnotationPresent(Component.class))
                .filter(clazz -> !clazz.isAnnotationPresent(Exclude.class))
                .forEach(clazz -> {
                    if(clazz.isInterface()) {
                        classes.stream()
                                .filter(clazz::isAssignableFrom)
                                .filter(implementation -> !implementation.isInterface())
                                .filter(implementation -> !implementation.isAnnotationPresent(Exclude.class))
                                .forEach(this::registerDependency);
                        return;
                    }
                    this.registerDependency(clazz);
                });

        //check for nested dependencies
        dependencies.forEach(dependency -> Arrays.stream(dependency.getClazz().getDeclaredFields())
                .filter(field -> !field.isAnnotationPresent(Exclude.class))
                .forEach(field -> {
                    Class<?> fieldType = field.getType();
                    dependencies.stream()
                            .filter(dep -> fieldType.isAssignableFrom(dep.getClazz()))
                            .findFirst().ifPresent(dependency::addDependency);
                }));

        //check for circular dependencies
        Set<Dependency<?>> visited = new HashSet<>();
        for (Dependency<?> dependency : dependencies) {
            if (!visited.contains(dependency)) {
                checkCircularDependency(dependency, visited, new HashSet<>());
            }
        }

        //initialize dependencies
        List<Dependency<?>> sortedDependencies = new ArrayList<>();
        dependencies.forEach(dependency -> initializeDependency(dependency, sortedDependencies));



        //all dependencies initialized at this point
        sortedDependencies.forEach(dependency -> {
            dependency.setInstance(createInstance(dependency.getClazz()));
            injectField(dependency.getInstance());

            Arrays.stream(dependency.getClazz().getMethods())
                    .filter(method -> method.isAnnotationPresent(PostConstruct.class))
                    .findAny()
                    .ifPresent(method -> {
                        try {
                            method.invoke(dependency.getInstance());
                        } catch (Exception e) {
                            throw new InjectorException("Error while invoking post construct method " + method.getName(), e);
                        }
                    });
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
            throw new InjectorException("Circular dependency detected involving " + dependency.getClazz());
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
    public <T> void injectField(T instance) {
        Arrays.stream(instance.getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Inject.class))
                .filter(field -> !Modifier.isFinal(field.getModifiers()))
                .forEach(field -> {
                    try {
                        String injectName = field.getAnnotation(Inject.class).value();
                        T toInject = (T) dependencies.stream()
                                .filter(dependency -> {
                                    if(field.getType().isInterface()) {
                                        return dependency.getClazz().isAssignableFrom(field.getType());
                                    }
                                    return dependency.getClazz().equals(field.getType()) || (!injectName.isEmpty() && injectName.equals(dependency.getName()));
                                })
                                .findFirst()
                                .orElseThrow(() -> new InjectorException("Could not find injectable for field " + field.getName()))
                                .getInstance();
                        if(toInject.getClass().equals(instance.getClass())) {
                            throw new InjectorException("Could not inject field " + field.getName() + " into itself");
                        }
                        field.setAccessible(true);
                        field.set(instance, toInject);
                    } catch (IllegalAccessException e) {
                        throw new InjectorException("Could not inject field " + field.getName(), e);
                    }
                });
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getInstance(Class<T> clazz) {
        if(clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
            List<Dependency<?>> implementations = dependencies.stream()
                    .filter(dependency -> clazz.isAssignableFrom(dependency.getClazz()))
                    .toList();
            if(implementations.isEmpty()) {
                throw new InjectorException("Could not find implementation for abstraction layer " + clazz.getName());
            } else if(implementations.size() > 1) {
                List<Dependency<?>> matchingDependencies = implementations.stream()
                        .filter(dependency -> dependency.getClazz().isAnnotationPresent(Primary.class))
                        .toList();
                if(matchingDependencies.size() > 1) {
                    throw new InjectorException("Found multiple implementations for abstraction layer " + clazz.getName() + " and multiple primary implementations");
                } else if(matchingDependencies.isEmpty()) {
                    throw new InjectorException("Could not find primary implementation for abstraction layer " + clazz.getName());
                }
                return (T) matchingDependencies.get(0).getInstance();
            } else {
                return (T) implementations.get(0).getInstance();
            }
        }
        return ((T) dependencies.stream()
                .filter(dependency -> dependency.getClazz().equals(clazz))
                .findFirst()
                .orElseThrow(() -> new InjectorException("Could not find injectable for class " + clazz.getName()))
                .getInstance());
    }

    @Override
    public void clearInjectables() {
        this.dependencies.clear();
    }


    @SuppressWarnings("unchecked")
    private <T> T instantiate(Dependency<T> dependency) {
        Constructor<T> constructor = (Constructor<T>) Arrays.stream(dependency.getClazz().getConstructors())
                .filter(constructorElement -> constructorElement.getParameterTypes().length == 0 || Arrays.stream(constructorElement.getParameterTypes())
                        .allMatch(element -> containsByClass(element) || (element.isInterface() && element.isAnnotationPresent(Component.class)))
                )
                .findAny()
                .orElseThrow(() -> new InjectorException("No matching constructor found for class " + dependency.getClazz().getName()));
        try {
            if(constructor.getParameterCount() == 0) {
                return constructor.newInstance();
            }

            return constructor.newInstance(Arrays.stream(constructor.getParameterTypes()).map(this::getInstance).toArray());
        } catch (Exception exception) {
            throw new InjectorException("Could not instantiate class " + dependency.getClazz().getName(), exception);
        }
    }

    private <T> void registerDependency(Class<T> clazz) {
        if(this.containsByClass(clazz)) {
            throw new InjectorException("Class " + clazz.getName() + " is already registered");
        }
        String injectableName = clazz.getAnnotation(Component.class) == null ? clazz.getSimpleName() : clazz.getAnnotation(Component.class).value();
        if(containsByName(injectableName)) {
            throw new InjectorException("Class with component name " + injectableName + " is already registered");
        }
        Dependency<T> dependency = new Dependency<>(injectableName, clazz, null);
        this.dependencies.add(dependency);
    }
    private <T> boolean containsByClass(Class<T> clazz) {
        return dependencies.stream().anyMatch(dependency -> dependency.getClazz().equals(clazz));
    }

    private boolean containsByName(String injectableName) {
        if(injectableName.isEmpty()) {
            return false;
        }
        return dependencies.stream().anyMatch(dependency -> dependency.getName().equals(injectableName));
    }
}
