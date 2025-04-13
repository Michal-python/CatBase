package cat.michal.catbase.injector;

import cat.michal.catbase.injector.annotations.Inject;
import cat.michal.catbase.injector.exceptions.InjectorException;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

class InjectorInstanceFactory {
    private final CatBaseInjector injector;
    private final InjectionContext injectionContext;

    public InjectorInstanceFactory(CatBaseInjector injector, InjectionContext injectionContext) {
        this.injector = injector;
        this.injectionContext = injectionContext;
    }

    <T> T createInstance(Dependency<T> dependency) {
        if(dependency.getProvideMethod() == null) {
            return constructorInjection(dependency);
        } else {
            return provideInstance(dependency.getProvideMethod());
        }
    }

    private <T> T constructorInjection(Dependency<T> dependency) {
        Constructor<T> constructor = this.injectionContext.getConstructor(dependency)
                .orElseThrow(() -> new InjectorException("Could not find valid constructor for " + dependency.getClazz()));

        try {
            if(constructor.getParameterCount() == 0) {
                return constructor.newInstance();
            }

            AtomicInteger i = new AtomicInteger();
            return constructor.newInstance( Arrays.stream(constructor.getAnnotatedParameterTypes())
                    .map(param -> {
                        Object instance = mapParameterTypeInstance(param, constructor.getParameterAnnotations()[i.get()]);
                        i.getAndIncrement();
                        return instance;
                    })
                    .toArray() );
        } catch (Exception exception) {
            throw new InjectorException("Could not create instance of class " + dependency.getClazz().getName(), exception);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T provideInstance(Method method) {
        try {
            AtomicInteger i = new AtomicInteger();
            return (T) method.invoke(this.injector.getInstance(method.getDeclaringClass()), Arrays.stream(method.getAnnotatedParameterTypes())
                    .map(param -> {
                        Object instance = mapParameterTypeInstance(param, method.getParameterAnnotations()[i.get()]);
                        i.getAndIncrement();
                        return instance;
                    })
                    .toArray() );
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new InjectorException("Error while invoking " + method.getName(), e);
        }
    }

    private Object mapParameterTypeInstance(AnnotatedType clazz, Annotation[] annotations) {
        Inject inject = (Inject) Arrays.stream(annotations)
                .filter(annotation -> annotation.annotationType().equals(Inject.class))
                .findFirst()
                .orElse(null);
        if(inject != null) {
            return injector.getInjectionExecutor().getValueToInject(clazz.getType(), inject.value());
        }
        return injector.getInjectionExecutor().getValueToInject(clazz.getType(), null);
    }

    @SuppressWarnings("unchecked")
    <T> T createInstance(Class<T> clazz) {
        if(clazz.isEnum()) {
            throw new InjectorException("Enums (" + clazz.getSimpleName() + ") are not supported");
        }

        return constructorInjection((Dependency<? extends T>) this.injectionContext.getDependencies().stream()
                .filter(dependency -> dependency.getClazz().equals(clazz))
                .findAny()
                .orElseThrow(() -> new InjectorException("Dependency for class " + clazz.getName() + " not found. Consider adding @Component annotation"))
        );
    }

}
