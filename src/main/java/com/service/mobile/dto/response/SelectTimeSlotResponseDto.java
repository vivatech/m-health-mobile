package com.service.mobile.dto.response;

import com.service.mobile.dto.dto.ConsultDetailSummaryDto;
import com.service.mobile.dto.dto.ProfileDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SelectTimeSlotResponseDto {
    private Map<String, Object> userdata;
    private ConsultDetailSummaryDto summary;
    private List<PaymentMethodResponse.Option> payment_method;
}
