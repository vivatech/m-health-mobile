package com.service.mobile.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class HealthTipPackageListRequest {
    private Integer user_id;
    private String name;
    private Integer page;
    private Integer cat_ids;
    private Boolean sort_by_price;
}
