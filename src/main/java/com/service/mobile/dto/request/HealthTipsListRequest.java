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
    private String user_id;
    private String title;
    private String category_id;
    private String package_id;
    private String page;
}
