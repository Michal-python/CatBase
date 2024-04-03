package cat.michal.catbase.injector;

import cat.michal.catbase.injector.annotations.*;
import cat.michal.catbase.injector.exceptions.InjectorException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public class DefaultInjector implements Injector {
    private final List<Injectable<?>> injectables;
    private final List<Class<?>> classes;


    public DefaultInjector(String packagePath) {
        this(new ArrayList<>(), packagePath);
    }

    private DefaultInjector(List<Injectable<?>> injectables, String packagePath) {
        this.injectables = injectables;
        this.classes = findAllClasses(packagePath).stream().toList();

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

        return instantiate(clazz);
    }

    @Override
    public List<Injectable<?>> getAll() {
        return Collections.unmodifiableList(injectables);
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
                                .forEach(this::registerInjectable);
                        return;
                    }
                    this.registerInjectable(clazz);
                });

        injectables.forEach(injectable -> inject(injectable.getInstance()));
    }

    @Override
    public <T> void inject(T instance) {
        Arrays.stream(instance.getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Inject.class))
                .filter(field -> !Modifier.isFinal(field.getModifiers()))
                .forEach(field -> {
                    try {
                        String injectName = field.getAnnotation(Inject.class).value();
                        Object toInject = injectables.stream()
                                .filter(injectable -> {
                                    if(field.getType().isInterface()) {
                                        return injectable.getClazz().isAssignableFrom(field.getType());
                                    }
                                    return injectable.getClazz().equals(field.getType()) || (!injectName.isEmpty() && injectName.equals(injectable.getName()));
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
            List<Injectable<?>> implementations = injectables.stream()
                    .filter(injectable -> clazz.isAssignableFrom(injectable.getClazz()))
                    .toList();
            if(implementations.isEmpty()) {
                throw new InjectorException("Could not find implementation for abstraction layer " + clazz.getName());
            } else if(implementations.size() > 1) {
                List<Injectable<?>> matchingInjectables = implementations.stream()
                        .filter(injectable -> injectable.getClazz().isAnnotationPresent(Primary.class))
                        .toList();
                if(matchingInjectables.size() > 1) {
                    throw new InjectorException("Found multiple implementations for abstraction layer " + clazz.getName() + " and multiple primary implementations");
                } else if(matchingInjectables.isEmpty()) {
                    throw new InjectorException("Could not find primary implementation for abstraction layer " + clazz.getName());
                }
                return (T) matchingInjectables.get(0).getInstance();
            } else {
                return (T) implementations.get(0).getInstance();
            }
        }
        return ((T) injectables.stream()
                .filter(injectable -> injectable.getClazz().equals(clazz))
                .findFirst()
                .orElseThrow(() -> new InjectorException("Could not find injectable for class " + clazz.getName()))
                .getInstance());
    }

    @Override
    public void clearInjectables() {
        this.injectables.clear();
    }


    @SuppressWarnings("unchecked")
    private <T> T instantiate(Class<?> clazz) {
        Constructor<T> constructor = (Constructor<T>) Arrays.stream(clazz.getConstructors())
                .filter(constructorElement -> constructorElement.getParameterTypes().length == 0 || Arrays.stream(constructorElement.getParameterTypes())
                        .allMatch(element -> containsByClass(element) || (element.isInterface() && element.isAnnotationPresent(Component.class)))
                )
                .findAny()
                .orElseThrow(() -> new InjectorException("No matching constructor found for class " + clazz.getName()));
        try {
            if(constructor.getParameterCount() == 0) {
                return constructor.newInstance();
            }
            return constructor.newInstance(Arrays.stream(constructor.getParameterTypes()).map(parameter -> {
                try {
                    return getInstance(parameter);
                } catch (InjectorException ignored) {
                    return createInstance(parameter);
                }
            }).toArray());
        } catch (Exception exception) {
            throw new InjectorException("Could not instantiate class " + clazz.getName(), exception);
        }
    }

    private <T> void registerInjectable(Class<T> clazz) {
        if(this.containsByClass(clazz)) {
            throw new InjectorException("Class " + clazz.getName() + " is already registered");
        }
        String injectableName = clazz.getAnnotation(Component.class) == null ? clazz.getSimpleName() : clazz.getAnnotation(Component.class).value();
        if(containsByName(injectableName)) {
            throw new InjectorException("Class with component name " + injectableName + " is already registered");
        }
        Injectable<T> injectable = new Injectable<>(injectableName, clazz, createInstance(clazz));
        this.injectables.add(injectable);
        Arrays.stream(clazz.getMethods())
                .filter(method -> method.isAnnotationPresent(PostConstruct.class))
                .findAny()
                .ifPresent(method -> {
                    try {
                        method.invoke(injectable.getInstance());
                    } catch (Exception e) {
                        throw new InjectorException("Error while invoking post construct method " + method.getName(), e);
                    }
                });
    }

    private Set<Class<?>> findAllClasses(String packageName) {
        InputStream stream = ClassLoader.getSystemClassLoader()
                .getResourceAsStream(packageName.replaceAll("[.]", "/"));
        assert stream != null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        return reader.lines()
                .filter(line -> line.endsWith(".class"))
                .map(line -> {
                    try {
                        return Class.forName(packageName + "." + line.substring(0, line.lastIndexOf('.')));
                    } catch (ClassNotFoundException ignored) {
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private <T> boolean containsByClass(Class<T> clazz) {
        return injectables.stream().anyMatch(injectable -> injectable.getClazz().equals(clazz));
    }

    private boolean containsByName(String injectableName) {
        if(injectableName.isEmpty()) {
            return false;
        }
        return injectables.stream().anyMatch(injectable -> injectable.getName().equals(injectableName));
    }
}
