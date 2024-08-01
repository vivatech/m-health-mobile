package com.service.mobile.dto.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProcessPaymentRequest {
    private Integer user_id;
    private Integer nurse_mobile;
    private Integer search_id;
    private String currency;
    private String payment_method;
    private Integer state_id;
    private Integer service_id;
    private String payment_number;
}
