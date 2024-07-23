package com.service.mobile.dto.response;

import com.service.mobile.dto.OfferInformationDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OfferResponseDTO {
    private List<OfferInformationDTO> offer_information;
    private String note_message;
    private String coupon_code;
}
