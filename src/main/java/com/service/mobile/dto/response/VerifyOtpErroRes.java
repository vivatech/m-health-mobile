package com.service.mobile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.service.mobile.config.Constants.SUCCESS;
import static com.service.mobile.config.Constants.SUCCESS_CODE;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VerifyOtpErroRes {
    private String status = SUCCESS_CODE;
    private Boolean is_active = false;
    private String message = SUCCESS_CODE;
    private Object data;
}
