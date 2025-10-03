package org.example.bci.visualizer.properties;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Loads properties from a file into a HashMap.
 */
public class PropertyLoader {

    /**
     * Reads a properties file into a HashMap.
     *
     * @param filePath path to the properties file.
     * @return HashMap with the properties.
     */
    public static Map<String, String> get(String filePath) {

        URL url = PropertyLoader.class.getResource(filePath);
        assert url != null;

        Properties properties = new Properties();
        Map<String, String> hashMap = new HashMap<>();

        try (FileInputStream fis = new FileInputStream(url.getPath())) {
            // Load the properties from the file
            properties.load(fis);

            // Iterate over the properties and put them into the HashMap
            for (String key : properties.stringPropertyNames()) {
                hashMap.put(key, properties.getProperty(key));
            }
        } catch (IOException e) {
            System.err.println("Error reading properties file: " + e.getMessage());
            e.printStackTrace();
        }
        return hashMap;
    }
}
