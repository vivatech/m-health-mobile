package com.service.mobile.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MobileReleaseRequest {
    private String app_version;
    private String device_type;
}
