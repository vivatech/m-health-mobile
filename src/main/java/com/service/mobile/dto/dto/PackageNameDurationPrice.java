package com.service.mobile.dto.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PackageNameDurationPrice {
    private String package_name;
    private Integer package_duration;
    private String package_price;
}
