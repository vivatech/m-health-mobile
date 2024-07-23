package com.service.mobile.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OfferInformationDTO {
    private String discount_amount;
    private String discount_type;
    private String discount_for;
    private String offer_text;
}
