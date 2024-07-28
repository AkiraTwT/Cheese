package akira.util;

import java.io.*;
import java.nio.file.Files;
import java.util.Objects;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Config {
    private static JSONObject config;
    private static final File FILE = new File("config.json");
    private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);

    public static void load() {
        try {
            if (!FILE.exists()) {
                try (InputStream in = Objects.requireNonNull(Config.class.getClassLoader().getResourceAsStream("config.json"))) {
                    Files.copy(in, FILE.toPath());
                }
            }
            try (FileReader reader =  new FileReader(FILE)) {
                config = new JSONObject(new JSONTokener(reader));
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load configuration: {}", e.getMessage());
        }
    }

    public static <T> void update(String path, T newValue) {
        if (path == null || path.isEmpty() || newValue == null) {
            LOGGER.warn("Invalid path or value: key={}, value={}", path, newValue);
            return;
        }

        String[] keys = path.split("\\.");
        JSONObject json = config;

        for (int i = 0; i < keys.length - 1; i++) {
            json = json.optJSONObject(keys[i]);
            if (json == null) {
                return;
            }
        }
        json.put(keys[keys.length - 1], newValue);
        saveConfig();
    }

    private static void saveConfig() {
        try (FileWriter fileWriter = new FileWriter(FILE)) {
            fileWriter.write(config.toString(4));
        } catch (IOException e) {
            LOGGER.error("Error writing to config.json: {}", e.getMessage());
        }
    }

    public static Object get(String path) {
        Object json = config;

        for (String key : path.split("\\.")) {
            try {
                json = ((JSONObject) json).get(key);
            } catch (Exception e) {
                return null;
            }
        }
        return json;
    }

    public static String getString(String path) {
        return get(path) instanceof String str ? str : null;
    }

    public static Integer getInteger(String path) {
        return get(path) instanceof Integer integer ? integer : null;
    }
}