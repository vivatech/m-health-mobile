package com.service.mobile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentServiceTypeResponse {
    private String consultation;
    private String lab;
    private String healthtip;
    private String load_wallet_balance;
    private String pharmacy;
    private String nurse_on_demand;
    private String lab_cart;
}
