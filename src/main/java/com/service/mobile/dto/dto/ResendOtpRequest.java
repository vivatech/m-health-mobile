package com.service.mobile.dto.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ResendOtpRequest {
    private String contact_number;
    private String is_registered;
    private String otp_code;
}
