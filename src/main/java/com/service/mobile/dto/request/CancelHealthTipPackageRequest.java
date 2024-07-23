package com.service.mobile.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CancelHealthTipPackageRequest {
    private Integer user_id;
    private Integer package_id;
    private Integer purchased_package_user_id;
}
