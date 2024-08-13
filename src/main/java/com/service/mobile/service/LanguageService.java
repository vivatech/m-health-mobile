package com.service.mobile.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


@Service
public class LanguageService {
    @Autowired
    private MessageSource messageSource;
    private MessageSourceAccessor accessor;

    @Value("${messages.properties.location}")
    private String propertiesPath;


    private Map<String, Properties> propertiesMap = new HashMap<>();

    public String getMessage(String key, String language, Object... args) {
        Properties properties = propertiesMap.get(language);
        if (properties == null) {
            properties = loadProperties(language);
            propertiesMap.put(language, properties);
        }

        String message = properties.getProperty(key);
        if (message == null && !"en".equalsIgnoreCase(language)) {
            // Fallback to English if the message is not found in the requested language
            properties = propertiesMap.get("en");
            if (properties == null) {
                properties = loadProperties("en");
                propertiesMap.put("en", properties);
            }
            message = properties.getProperty(key);
        }

        if (message != null && args != null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                String replacement = args[i] != null ? args[i].toString() : "null";
                message = message.replace("{" + i + "}", replacement);
            }
        }

        return message;
    }

    private Properties loadProperties(String locale) {
        Properties properties = new Properties();

        String propertiesFilePath = propertiesPath+ "messages" + getLocaleSuffix(locale) + ".properties";

        Path path = Paths.get(propertiesFilePath);
        try (InputStream inputStream = Files.newInputStream(path)) {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Unable to load properties file: " + propertiesFilePath, e);
        }
        return properties;
    }

    private String getLocaleSuffix(String locale) {
        if (locale != null && !locale.isEmpty() && !"en".equalsIgnoreCase(locale)) {
            return "_"+locale;
        }
        return "";
    }

    public String getMessage(String key) {
        return getMessage(key, LocaleContextHolder.getLocale().getLanguage(), null);
    }
    public String gettingMessages(String key, Object... args) {
        return getMessage(key, LocaleContextHolder.getLocale().getLanguage(), args);
    }
}


