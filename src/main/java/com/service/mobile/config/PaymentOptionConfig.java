package com.service.mobile.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class PaymentOptionConfig {

    @Value("#{${payment.option.currency}}")
    private Map<String, String> currency;

    @Value("#{${payment.option.method}}")
    private Map<String, String> paymentMethod;

    public Map<String, String> getCurrency() {
        return currency;
    }

    public Map<String, String> getPaymentMethod() {
        return paymentMethod;
    }
}
