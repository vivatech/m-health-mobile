package com.service.mobile.dto.response;

import com.service.mobile.dto.dto.ConsultDetailSummaryDto;
import com.service.mobile.dto.dto.ProfileDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SelectTimeSlotResponseDto {
    private ProfileDto userdata;
    private List<ConsultDetailSummaryDto> summary;
    private List<PaymentMethodResponse.Option> payment_method;
}
