package com.service.mobile.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

/**
 *
 * @author jainab
 */
@Configuration
@PropertySource("${messages.properties.location}sms.properties")
public class SmsConfig {

    @Autowired
    private Environment env;

    public String getProperty(String key)
    {
        return env.containsProperty(key) ? env.getProperty(key) : "";
    }

}
