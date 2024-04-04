package cat.michal.catbase.injector;

import cat.michal.catbase.injector.exceptions.InjectorException;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassFinder {
    public static Set<Class<?>> findAllClasses(String packageName) {
        Set<Class<?>> classes = new HashSet<>();
        String packagePath = packageName.replace('.', '/');
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = classLoader.getResources(packagePath);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                String protocol = resource.getProtocol();
                if (protocol.equals("file")) {
                    classes.addAll(findClasses(packageName, resource.toURI()));
                } else if (protocol.equals("jar")) {
                    classes.addAll(findClassesInJar(packageName, resource));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new InjectorException("Could not index classes in package", e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return classes;
    }

    private static Set<Class<?>> findClassesInJar(String packageName, URL jarURL) throws IOException {
        Set<Class<?>> classes = new HashSet<>();
        String jarPath = "jar:" + jarURL.getPath();

        try (JarFile jarFile = new JarFile(URLDecoder.decode(jarPath, StandardCharsets.UTF_8))) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    String className = entry.getName().replace(File.separator, ".")
                            .replaceAll("\\.class$", "");
                    if (className.startsWith(packageName)) {
                        try {
                            classes.add(Class.forName(className));
                        } catch (ClassNotFoundException e) {
                            throw new InjectorException("Could not find class", e);
                        }
                    }
                }
            }
        }
        return classes;
    }


    private static Set<Class<?>> findClasses(String packageName, URI packageURI) throws IOException {
        Set<Class<?>> classes = new HashSet<>();
        try {
            Path path;
            String packagePath = packageURI.getPath();
            if (packageURI.getScheme() == null) {
                throw new InjectorException("Scheme is null");
            } else if (packageURI.getScheme().equals("file")) {
                path = Paths.get(packageURI);
            } else {
                FileSystem fs = FileSystems.newFileSystem(packageURI, Collections.emptyMap());
                path = fs.getPath(packagePath);
            }
            Files.walk(path)
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".class"))
                    .forEach(p -> {
                        String className = packageName + "." + getClassName(packageURI, p);
                        try {
                            classes.add(Class.forName(className));
                        } catch (ClassNotFoundException e) {
                            throw new InjectorException("Could not find class", e);
                        }
                    });
        } catch (IOException e) {
            throw new InjectorException("Could not index classes in package", e);
        }
        return classes;
    }

    private static String getClassName(URI packageUri, Path filePath) {
        var clazz = packageUri.relativize(filePath.toUri()).normalize().toString();
        clazz = clazz.replace('/', '.');
        return clazz.substring(0, clazz.length() - 6);
    }
}
