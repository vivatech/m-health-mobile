package com.service.mobile.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class HealthTipPackageBookingRequest {
    private Integer user_id;
    private Integer package_id;
    private String payment_method;
    private String type;
    private String currency_option;
    private String coupon_code;
    private String payment_number;
}
