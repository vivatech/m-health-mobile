package com.service.mobile.dto.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BalanceResponseDTO {
    private String status;
    private String message;
    private String countryCode;
    private String contactNumber;
    private Float totalMoney;
    private String data;
}
