package cat.michal.catbase.injector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class ClassFinder {
    public static List<Class<?>> findAllClasses(String packageName) {
        return findAllClassesPaths(packageName).stream()
                .map(ClassFinder::getClass)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static List<String> findAllClassesPaths(String packageName) {
        URL resource = ClassLoader.getSystemClassLoader().getResource(packageName.replace('.', '/'));
        assert resource != null;
        if (resource.getProtocol().startsWith("jar")) {
            return enumerateJar(packageName, resource);
        }

        InputStream stream = ClassLoader.getSystemClassLoader()
                .getResourceAsStream(packageName.replace('.', '/'));

        assert stream != null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        List<String> classes = new ArrayList<>();
        reader.lines()
                .forEach(line -> {
                    if(line.endsWith(".class")) {
                        classes.add(packageName + "." + line);
                    } else {
                        classes.addAll(findAllClassesPaths(packageName + "." + line));
                    }
                });

        return classes;
    }

    private static List<String> enumerateJar(String packageName, URL resource) {
        try {
            String[] parts = resource.toString().split("!/");
            String jarFilePath = parts[0].substring("jar:file:".length());
            String packagePath = packageName.replace('.', '/');

            List<String> classes = new ArrayList<>();

            try (JarFile jar = new JarFile(jarFilePath)) {
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (entry.getName().startsWith(packagePath + "/") && entry.getName().endsWith(".class")) {
                        classes.add(entry.getName().replace('/', '.'));
                    }
                }
            }

            return classes;
        } catch (IOException e) {
            return List.of();
        }
    }

    private static Class<?> getClass(String className) {
        try {
            return Class.forName(className.substring(0, className.lastIndexOf('.')));
        } catch (ClassNotFoundException ignored) {
        }
        return null;
    }
}