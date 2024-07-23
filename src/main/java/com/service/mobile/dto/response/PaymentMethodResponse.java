package com.service.mobile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentMethodResponse {
    private String status;
    private String message;
    private List<Option> data;
    private List<Option> currencyOption;

    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Option {
        private String value;
        private String title;

        // Constructors, Getters and Setters
    }

}
