package de.thegame4craft.core;

import com.moandjiezana.toml.Toml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class PropertiesManager {
    private final HashMap<String, String> properties = new HashMap<>();
    private final Toml prop;

    public PropertiesManager(Path p) {
        prop = new Toml().read(p.toFile());
    }

    public static void generateServerProperties(Path p) {
        if(!p.toFile().exists()) {
            InputStream is = PropertiesManager.class.getResourceAsStream("/default.properties");
            if(is == null) {
                throw new RuntimeException("Failed to load default properties");
            }
            try {
                Files.copy(is, p);
                is.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public PropertiesManager(Toml t) {
        prop = t;
    }

    public HashMap<String, String> getCachedProperties() {
        return properties;
    }


    public String getProperty(String key) {
        if(properties.containsKey(key)) {
            return properties.get(key);
        }
        final String v = prop.getString(key);
        properties.put(key, v);
        return v;
    }

    public double getDouble(String key) {
        if(properties.containsKey(key)) {
            return Double.parseDouble(properties.get(key));
        }
        final double v = prop.getDouble(key);
        properties.put(key, String.valueOf(v));
        return v;
    }
    public float getFloat(String key) {
        if(properties.containsKey(key)) {
            return Float.parseFloat(properties.get(key));
        }
        final float v = prop.getDouble(key).floatValue();
        properties.put(key, String.valueOf(v));
        return v;
    }

    public Boolean getBoolean(String key) {
        if(properties.containsKey(key)) {
            return Boolean.parseBoolean(properties.get(key));
        }
        final boolean v = prop.getBoolean(key);

        properties.put(key, String.valueOf(v));
        return v;
    }
}
