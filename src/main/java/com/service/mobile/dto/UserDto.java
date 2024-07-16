package com.service.mobile.dto;

import lombok.Data;

@Data
public class UserDto {
    private String fullName;
    private String contactNumber;
    private String promoCode;
    private boolean termsAndCondition;
}
