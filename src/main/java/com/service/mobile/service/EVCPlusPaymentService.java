package com.service.mobile.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class EVCPlusPaymentService {

    @Value("${waafi.api.url}")
    private String waafiApiUrl;

    @Value("${waafi.payment.merchantUid}")
    private String merchantUid;

    @Value("${waafi.payment.userId}")
    private String userId;

    @Value("${waafi.payment.apiKey}")
    private String apiKey;

    @Value("${waafi.country.code}")
    private String countryCode;

    @Value("${waafi.project.name}")
    private String projectName;

    @Value("${waafi.currency.code}")
    private String currencyCode;

    public Map<String, Object> processPayment(String serviceName, Map<String, Object> transactionDetail, double totalAmount, String contactNumber, String patientId, String currencyOptions, String serviceType) throws JsonProcessingException {

        Map<String, Object> apiParams = new HashMap<>();
        apiParams.put("schemaVersion", "1.0");
        apiParams.put("requestId", Instant.now().getEpochSecond());
        apiParams.put("timestamp", Instant.now().getEpochSecond());
        apiParams.put("channelName", "WEB");
        apiParams.put("serviceName", serviceName);

        if ("API_REFUND".equals(serviceName)) {
            Map<String, Object> serviceParams = new HashMap<>();
            serviceParams.put("merchantUid", merchantUid);
            serviceParams.put("apiUserId", userId);
            serviceParams.put("apiKey", apiKey);
            serviceParams.put("userReferenceId", transactionDetail.get("ref_transaction_id"));
            serviceParams.put("transactionId", transactionDetail.get("ref_transaction_id"));
            serviceParams.put("amount", totalAmount);
            serviceParams.put("description", "Refunded from " + projectName + " for case id : " + transactionDetail.getOrDefault("reference_number", ""));
            serviceParams.put("referenceId", transactionDetail.get("reference_number"));

            apiParams.put("serviceParams", serviceParams);
        } else {
            Map<String, Object> serviceParams = new HashMap<>();
            serviceParams.put("merchantUid", merchantUid);
            serviceParams.put("apiUserId", userId);
            serviceParams.put("apiKey", apiKey);
            serviceParams.put("paymentMethod", "mwallet_account");
            serviceParams.put("payerInfo", Map.of("accountNo", countryCode + contactNumber));
            serviceParams.put("transactionInfo", Map.of(
                    "invoiceId", Instant.now().getEpochSecond(),
                    "amount", totalAmount,
                    "description", "Payment from " + projectName,
                    "referenceId", patientId,
                    "currency", currencyOptions
            ));

            apiParams.put("serviceParams", serviceParams);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(apiParams, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.exchange(waafiApiUrl, HttpMethod.POST, requestEntity, String.class);
        String responseBody = responseEntity.getBody();

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> response = objectMapper.readValue(responseBody, new TypeReference<>() {});

        // Logging
        log.info(String.format(
                "[PAYMENT_FROM : %s][REQUEST_MODE : orderPayment][REQUEST : %s][RESPONSE : %s][DATE : %s]%n%n",
                countryCode + contactNumber,
                apiParams,
                objectMapper.writeValueAsString(response),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ").withZone(ZoneId.systemDefault()).format(Instant.now())
        ));


        if (response != null) {
            if ("0".equals(response.get("errorCode"))) {
                Map<String, Object> customResponse = new HashMap<>();
                customResponse.put("transactionId", response.get("params.transactionId"));
                customResponse.put("issuerTransactionId", response.get("params.issuerTransactionId"));
                customResponse.put("referenceId", response.get("params.referenceId"));
                customResponse.put("currency", currencyCode);
                customResponse.put("state", response.get("responseMsg"));

                if ("API_REFUND".equals(serviceName)) {
                    // Notification will not be sent
                } else if (!"NOD".equals(serviceType)) {
                    // NOTE-TODO: Notification will sent
                    // HelperComponent.sendPaymentNotification(patientId, totalAmount, "SYSTEM_PAYMENT_SUCCESS", currencyOptions);
                }
                return Map.of("data", customResponse, "status", 200);
            }
            else {
                if(!"API_REFUND".equals(serviceName)){
                    // NOTE-TODO: Notification will sent
                    // sendPaymentNotification(patientId, totalAmount, "SYSTEM_PAYMENT_FAILED", currencyOptions);
                }
                String errorMessage = getErrorMessage(response);
                return Map.of("message", errorMessage, "status", 100);
            }

        } else {
            if(!"API_REFUND".equals(serviceName)){
                // NOTE-TODO: Notification will sent
                // sendPaymentNotification(patientId, totalAmount, "SYSTEM_PAYMENT_FAILED", currencyOptions);
            }
            return Map.of("message", "Payment failed", "status", 100);
        }
    }

    private String getErrorMessage(Map<String, Object> response) {
        String errorCode = (String) response.get("errorCode");
        return switch (errorCode) {
            case "E10205" -> "Incorrect PIN";
            case "5310" -> "Payment cancelled or rejected";
            default -> "Payment failed";
        };
    }
}
