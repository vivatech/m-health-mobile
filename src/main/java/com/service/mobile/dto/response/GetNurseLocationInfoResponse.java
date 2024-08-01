package com.service.mobile.dto.response;

import com.service.mobile.dto.dto.IdNameMobileDto;
import com.service.mobile.dto.dto.NamePriceDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GetNurseLocationInfoResponse {
    private List<NamePriceDto> services;
    private String services_price;
    private String distance_fee;
    private String amount;
    private String slsh_amount;
    private IdNameMobileDto nurse;
    private Integer state_id;
}
