package cat.michal.catbase.injector;

import cat.michal.catbase.injector.annotations.Inject;
import cat.michal.catbase.injector.annotations.PostConstruct;
import cat.michal.catbase.injector.annotations.PreDestroy;
import cat.michal.catbase.injector.exceptions.InjectorException;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;

class InjectionExecutor {
    private final InjectionContext injectionContext;
    private final CatBaseInjector injector;

    public InjectionExecutor(InjectionContext injectionContext, CatBaseInjector injector) {
        this.injectionContext = injectionContext;
        this.injector = injector;
    }

    void invokePostConstruct(Dependency<?> dependency) {
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
    }

    void invokePreDestroy(Dependency<?> dependency) {
        Arrays.stream(dependency.getClazz().getMethods())
                .filter(method -> method.isAnnotationPresent(PreDestroy.class))
                .findAny()
                .ifPresent(method -> {
                    try {
                        method.invoke(dependency.getInstance());
                    } catch (Exception e) {
                        throw new InjectorException("Error while invoking pre destroy method " + method.getName(), e);
                    }
                });
    }

    <T> void injectField(T instance) {
        Arrays.stream(instance.getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Inject.class))
                .filter(field -> !Modifier.isFinal(field.getModifiers()))
                .forEach(field -> {
                    try {
                        String injectName = field.getAnnotation(Inject.class).value();


                        field.setAccessible(true);
                        field.set(instance, getValueToInject(field.getGenericType(), injectName));
                    } catch (InjectorException e) {
                        throw new InjectorException("Field " + field.getName() + " annotated with @Inject even though its type is not a dependency.", e);
                    } catch (IllegalAccessException e) {
                        throw new InjectorException("Could not inject field " + instance.getClass().getSimpleName() + "." + field.getName(), e);
                    }
                });
    }

    Object getValueToInject(Type type, String dependencyName) {
        if(type instanceof ParameterizedType parameterType && Collection.class.isAssignableFrom(((Class<?>) parameterType.getRawType()))) {
            Type[] actualTypeArguments = parameterType.getActualTypeArguments();
            if (actualTypeArguments.length != 1 || !(actualTypeArguments[0] instanceof Class)) {
                throw new InjectorException("Invalid type to inject " + type.getTypeName() + ". Expected a Class but got " + actualTypeArguments[0]);
            }

            return this.injector.getInstancesOfType((Class<?>) actualTypeArguments[0]);
        }
        if (!(type instanceof Class<?> clazz)) {
            throw new InjectorException("Invalid type to inject " + type.getTypeName() + ". Expected a Class but got " + type);
        }

        return this.injectionContext.findBestMatchingDependency(clazz, dependencyName)
                .orElseThrow(() -> new InjectorException("Type " + clazz.getName() + " is not a dependency"))
                .getInstance();
    }
}
