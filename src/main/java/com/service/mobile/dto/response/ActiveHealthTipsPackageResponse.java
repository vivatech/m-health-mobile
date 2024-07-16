package com.service.mobile.dto.response;

import com.service.mobile.dto.enums.PackageType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ActiveHealthTipsPackageResponse {
    private Integer package_id;
    private String package_name;
    private String image;
    private String package_price;
    private PackageType package_type=PackageType.Free;
    private String expired_at;
}
