package com.service.mobile.storage;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

@ConfigurationProperties(value = "storage")
public class StorageProperties {

    /**
     * Folder location for storing files
     */
    @Value("${storage.location}")
    public String location;
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
