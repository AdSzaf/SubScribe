package com.example.subscribe.utils;

import java.io.*;
import java.util.Properties;

public class ConfigManager {
    private static final String CONFIG_FILE_NAME = "config.properties";
    private static final Properties properties = new Properties();

    public static void loadConfig() {
        File configFile = new File(CONFIG_FILE_NAME);

        // If not exists, copy from resources
        if (!configFile.exists()) {
            try (InputStream in = ConfigManager.class.getResourceAsStream("/config.properties");
                 OutputStream out = new FileOutputStream(configFile)) {
                if (in != null) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        out.write(buffer, 0, len);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Now always load from file
        try (InputStream input = new FileInputStream(configFile)) {
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public static void set(String key, String value) {
        properties.setProperty(key, value);
    }

    public static void saveConfig() {
        try (OutputStream output = new FileOutputStream(CONFIG_FILE_NAME)) {
            properties.store(output, null);
        } catch (IOException io) {
            io.printStackTrace();
        }
    }
}