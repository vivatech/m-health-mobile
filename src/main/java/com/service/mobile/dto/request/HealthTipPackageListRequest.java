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
    private String cat_ids;
    private String sort_by_price;
    private Float from_price;
    private Float to_price;
}
