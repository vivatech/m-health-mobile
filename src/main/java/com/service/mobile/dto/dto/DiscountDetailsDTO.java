package com.service.mobile.dto.dto;

import com.service.mobile.dto.enums.OfferType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DiscountDetailsDTO {
    private String message;
    private String status;
    private String discount_amount_with_currency;
    private String discount_amount_with_usd_currency;
    private String discount_amount_with_slsh_currency;
    private String alert_msg_usd;
    private String alert_msg_slsh;
    private Float discount_amount_slsh;
    private Float discount_amount;
    private OfferType type;
    private Integer coupon_id;
}
