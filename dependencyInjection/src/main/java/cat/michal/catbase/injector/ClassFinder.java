package cat.michal.catbase.injector;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

public class ClassFinder {
    public static Set<Class<?>> findAllClasses(String packageName) {
        Set<Class<?>> classes = new HashSet<>();
        String packagePath = packageName.replace('.', '/');
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = classLoader.getResources(packagePath);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                if (resource.getProtocol().equals("file")) {
                    File packageDir = new File(resource.getFile());
                    classes.addAll(findClasses(packageName, packageDir));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return classes;
    }

    private static Set<Class<?>> findClasses(String packageName, File directory) {
        Set<Class<?>> classes = new HashSet<>();
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    classes.addAll(findClasses(packageName + "." + file.getName(), file));
                } else if (file.getName().endsWith(".class")) {
                    try {
                        String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                        classes.add(Class.forName(className));
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return classes;
    }
}
