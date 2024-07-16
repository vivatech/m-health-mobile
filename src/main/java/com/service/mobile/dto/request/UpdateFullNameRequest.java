package com.service.mobile.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateFullNameRequest {
    private Integer user_id;
    private String fullName;
    private String promo_code_of;
    private String device_token;
}
