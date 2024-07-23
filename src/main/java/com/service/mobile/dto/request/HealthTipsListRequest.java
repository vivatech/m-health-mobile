package com.service.mobile.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class HealthTipsListRequest {
    private Integer user_id;
    private String title;
    private Integer category_id;
    private Integer package_id;
    private Integer page=0;
    private Integer size=5;
}
