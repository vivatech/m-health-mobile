package com.service.mobile.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class SDFSMSService {

    @Value("${sdf.sms.forward.url}")
    private String smsForwardUrl;

    @Value("${sdf.api.username}")
    private String apiUsername;

    @Value("${sdf.api.password}")
    private String apiPassword;

    @Value("${sdf.project.name}")
    private String projectName;

    @Value("${sdf.sms.from.number}")
    private String fromNumber;

    public void sendOTPSMS(String to, String msg) {
        String accessToken = Base64.getEncoder().encodeToString((apiUsername + ":" + apiPassword).getBytes());

        Map<String, Object> apiParams = new HashMap<>();
        apiParams.put("fromNumber", fromNumber);
        apiParams.put("toNumber", to);
        apiParams.put("message", msg);
        apiParams.put("timeToLiveSeconds", 240);
        apiParams.put("application", projectName);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        RestTemplate restTemplate = new RestTemplate();

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(apiParams, headers);

        restTemplate.postForEntity(smsForwardUrl, request, String.class);

        log.info("sms sent: {}, msisdn: {}", msg, to);
    }
}

