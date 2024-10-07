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
    private String country_code;
    private String contact_number;
    private Float total_money;
    private Object data;
}
