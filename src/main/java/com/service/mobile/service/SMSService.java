package com.service.mobile.service;

import com.service.mobile.config.SmsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SMSService {

    @Autowired
    SmsConfig config;

    public void sendSMS(String message,String notificationMsg){
        //NOTE complete this function
    }

    public String getValue(String key){
        return config.getProperty(key);
    }
}
