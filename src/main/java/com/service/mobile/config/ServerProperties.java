package com.service.mobile.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "server")
public class ServerProperties{
    @Value("${app.server.time.zone")
    private String time;
    // Getter and Setter
    @Setter
    @Getter
    private String timeZone = null;

}
