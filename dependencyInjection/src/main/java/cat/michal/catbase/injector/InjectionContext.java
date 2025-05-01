package cat.michal.catbase.injector;

import cat.michal.catbase.injector.annotations.*;
import cat.michal.catbase.injector.exceptions.InjectorException;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.*;
import java.util.*;

public class InjectionContext {
    private final List<Class<?>> classes;
    private final List<Dependency<?>> dependencies;

    public InjectionContext(Collection<String> packagePaths, ClassLoader classLoader) {
        this.classes = new ArrayList<>();
        this.dependencies = new ArrayList<>();
        ClassFinder classFinder = classLoader == null ? new ClassFinder() : new ClassFinder(classLoader);

        packagePaths.forEach(packagePath -> this.classes.addAll(classFinder.findAllClasses(packagePath)));
    }

    public void createDependencyTree() {
        this.classes.stream()
                .filter(clazz -> clazz.isAnnotationPresent(Component.class))
                .filter(clazz -> !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers()))
                .forEach(clazz -> {
                    this.dependencies.add(createDependency(clazz, null, null));
                    Arrays.stream(clazz.getMethods())
                            .filter(method -> method.isAnnotationPresent(Provide.class))
                            .forEach(method -> this.dependencies.add(createDependency(method.getReturnType(), method, method.getAnnotation(Provide.class).value())));

                    // Adding external dependencies
                    Arrays.stream(clazz.getFields())
                            .filter(field ->  Modifier.isStatic(field.getModifiers()))
                            .filter(field -> field.isAnnotationPresent(ExternalDependency.class))
                            .forEach(field -> {
                                Dependency<?> dependency = createDependency(field.getType(), null, field.getAnnotation(ExternalDependency.class).value());
                                field.setAccessible(true);
                                try {
                                    dependency.setInstance(field.get(null));
                                } catch (IllegalAccessException e) {
                                    throw new InjectorException("Could not get value of external dependency " + field.getName(), e);
                                }
                                this.dependencies.add(dependency);
                            });
                });
    }

    public List<Dependency<?>> getDependencies() {
        return this.dependencies;
    }

    /**
     * Method that finds the best corresponding dependency to provided class
     * If user provides an abstraction layer, it automatically matches it and tries to find a primary or a named implementation of it
     * @param clazz class your want to find dependency to
     * @return none if there is no dependency connected with your class
     * @param <T> class type
     *
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<Dependency<T>> findBestMatchingDependency(Class<T> clazz, @Nullable String dependencyName) {
        // class is an abstraction layer
        if(clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
            List<Dependency<?>> matchingDependencies = this.dependencies.stream()
                    .filter(dependency -> clazz.isAssignableFrom(dependency.getClazz()))
                    .toList();
            if(matchingDependencies.isEmpty()) {
                throw new InjectorException("Could not find dependency for type " + clazz.getName());
            }
            if(matchingDependencies.size() > 1) {
                List<Dependency<?>> nameFiltered = matchingDependencies.stream()
                        .filter(dependency -> (dependencyName == null || dependencyName.isEmpty()) || dependency.getName().equals(dependencyName))
                        .toList();
                if(nameFiltered.isEmpty()) {
                    throw new InjectorException("Could not find matching dependency for type "+ clazz.getName() + " with name " + dependencyName);
                }

                if(nameFiltered.size() > 1) {
                    List<Dependency<?>> primaryFiltered = nameFiltered.stream()
                            .filter(dependency -> dependency.getClazz().isAnnotationPresent(Primary.class))
                            .toList();
                    if(primaryFiltered.isEmpty()) {
                        throw new InjectorException("Could not find primary implementation for type "+ clazz.getName());
                    }
                    if(primaryFiltered.size() > 1) {
                        throw new InjectorException("Multiple primary implementations found for type "+ clazz.getName());
                    }
                    return Optional.of((Dependency<T>) primaryFiltered.get(0));
                } else {
                    return Optional.of((Dependency<T>) nameFiltered.get(0));
                }
            } else {
                return Optional.of((Dependency<T>) matchingDependencies.get(0));
            }
        }

        return this.dependencies.stream()
                .filter(dependency -> dependency.getClazz().equals(clazz))
                .filter(dependency -> (dependencyName == null || dependencyName.isEmpty()) || dependency.getName().equals(dependencyName))
                .map(dependency -> (Dependency<T>) dependency)
                .findFirst();
    }

    @SuppressWarnings("unchecked")
    <T> List<Dependency<T>> findAllDependsOfType(Type type, String dependencyName) {
        List<Dependency<T>> depends = new ArrayList<>();
        if(type instanceof ParameterizedType parameterizedType && Collection.class.isAssignableFrom(((Class<?>) parameterizedType.getRawType())))  {
            Type[] arguments = parameterizedType.getActualTypeArguments();
            if(arguments.length != 1 || !(arguments[0] instanceof Class<?> abstractionType)) {
                throw new InjectorException("Invalid generic parameters with the collection class " + type.getTypeName());
            }
            this.dependencies.stream()
                    .filter(depend -> abstractionType.isAssignableFrom(depend.getClazz()))
                    .forEach(depend -> depends.add((Dependency<T>) depend));
        } else if (type instanceof Class<?> clazz) {
            this.findBestMatchingDependency((Class<T>) clazz, dependencyName).ifPresent(depends::add);
        } else {
            throw new InjectorException("Dependency with type " + type + " is not a valid class or a collection");
        }

        return depends;
    }


    @SuppressWarnings("unchecked")
    public <T> List<Dependency<T>> findDependants(Dependency<T> dependency) {
        List<Dependency<T>> dependants = new ArrayList<>();

        Optional<? extends Constructor<?>> validConstructor = getConstructor(dependency);
        validConstructor.ifPresent(constructor -> Arrays.stream(constructor.getAnnotatedParameterTypes())
                .forEach(type -> {
                    Inject inject = type.getAnnotation(Inject.class);

                    this.findAllDependsOfType(type.getType(), inject == null ? null : inject.value())
                            .forEach(dep -> dependants.add((Dependency<T>) dep));
                }));

        Arrays.stream(dependency.getClazz().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Inject.class))
                .forEach(field -> this.findAllDependsOfType(field.getGenericType(), field.getAnnotation(Inject.class).value())
                        .forEach(dep -> dependants.add((Dependency<T>) dep)));

        return dependants;
    }


    @SuppressWarnings("unchecked")
    public <T> Optional<Dependency<T>> getDependency(Class<T> clazz) {
        return this.dependencies.stream()
                .filter(dependency -> dependency.getClazz().equals(clazz))
                .map(dependency -> (Dependency<T>) dependency)
                .findFirst();
    }

    private <T> Dependency<T> createDependency(Class<T> clazz, Method provideMethod, String dependencyName) {
        if(this.containsByClass(clazz)) {
            throw new InjectorException("Dependency with type " + clazz.getName() + " has been added to injector context twice.");
        }

        if(dependencyName == null || dependencyName.isEmpty()) {
            if(provideMethod == null) {
                dependencyName = clazz.getAnnotation(Component.class) == null ? clazz.getSimpleName() : clazz.getAnnotation(Component.class).value();
            } else {
                String provideName = provideMethod.getAnnotation(Provide.class).value();
                dependencyName = provideName.isEmpty() ? provideMethod.getName() : provideName;
            }
        }

        Optional<Dependency<?>> depend = findByName(dependencyName);
        if(depend.isPresent()) {
            throw new InjectorException("Dependency with name '" + dependencyName + "' already exists (" + depend.get().getClass().getSimpleName() + ")");
        }
        return new Dependency<>(dependencyName, clazz, provideMethod);
    }


    private <T> boolean containsByClass(Class<T> clazz) {
        return this.dependencies.stream()
                .anyMatch(dependency -> dependency.getClazz().equals(clazz));
    }

    private Optional<Dependency<?>> findByName(String injectableName) {
        if(injectableName.isEmpty()) {
            return Optional.empty();
        }

        return this.dependencies.stream()
                .filter(dependency -> dependency.getName().equals(injectableName))
                .findAny();
    }


    @SuppressWarnings("unchecked")
    <T> Optional<Constructor<T>> getConstructor(Dependency<T> dependency) {
        List<Constructor<?>> injectConstructors = Arrays.stream(dependency.getClazz().getConstructors())
                .filter(constructor -> constructor.isAnnotationPresent(Inject.class))
                .toList();
        if(injectConstructors.isEmpty()) {
            return Arrays.stream(dependency.getClazz().getConstructors())
                    .filter(constructorElement -> constructorElement.getParameterTypes().length == 0
                            || Arrays.stream(constructorElement.getGenericParameterTypes()).allMatch(this::isValidInjectableType)
                    )
                    .map(constructor -> (Constructor<T>) constructor)
                    .findAny();
        } else {
            return Optional.of((Constructor<T>) injectConstructors.get(0));
        }
    }

    private boolean isValidInjectableType(Type type) {
        if(type instanceof ParameterizedType parameterType && Collection.class.isAssignableFrom(((Class<?>) parameterType.getRawType()))) {
            Type[] actualTypeArguments = parameterType.getActualTypeArguments();
            if (actualTypeArguments.length != 1 || !(actualTypeArguments[0] instanceof Class<?> actualClass)) {
                return false;
            }
            //  if class is an abstraction layer which usually should be, we need to check if there are any implementations present, if not, the type is not an injectable type
            if(actualClass.isInterface() || Modifier.isAbstract(actualClass.getModifiers())) {
                return this.dependencies.stream()
                        .anyMatch(dependency -> actualClass.isAssignableFrom(dependency.getClazz()));
            }

            return this.isValidInjectable(actualClass);
        }
        if (!(type instanceof Class<?> clazz)) {
            return false;
        }

        return this.isValidInjectable(clazz);
    }

    private boolean isValidInjectable(Class<?> clazz) {
        return this.findBestMatchingDependency(clazz, null).isPresent();
    }
}
