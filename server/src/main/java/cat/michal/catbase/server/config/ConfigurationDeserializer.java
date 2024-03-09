package cat.michal.catbase.server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ConfigurationDeserializer {
    public static ConfigurationInstance instance = new ConfigurationInstance();
    private final String configPath;
    private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    public ConfigurationDeserializer(String configPath) {
        this.configPath = configPath;
    }

    public void load() {
        try {
            String fileContent = String.join("\n", Files.readAllLines(Paths.get(".", configPath)));

            instance = mapper.readValue(fileContent, ConfigurationInstance.class);
        } catch (IOException exception) {
            throw new IllegalArgumentException("Invalid yaml in configuration file");
        }
    }

    public void generateDefault() {
        File file = new File(configPath);
        if(!configExists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            mapper.writeValue(file, instance);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public boolean configExists() {
        return Files.exists(Paths.get(".", configPath));
    }
}