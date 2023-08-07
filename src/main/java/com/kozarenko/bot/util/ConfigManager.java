package com.kozarenko.bot.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigManager {

    private static ConfigManager configManager;
    private final Properties properties;

    private ConfigManager() throws IOException {
        try (FileInputStream fis = new FileInputStream(Constants.CONFIG_FILE_PATH)) {
            properties = new Properties();
            properties.load(fis);
        }
    }

    public static ConfigManager instance() throws IOException {
        if (configManager == null) {
            configManager = new ConfigManager();
        }
        return configManager;
    }

    public String getProperty(String propertyName) {
        return properties.getProperty(propertyName);
    }
}
