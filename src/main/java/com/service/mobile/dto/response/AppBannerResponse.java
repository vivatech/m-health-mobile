package com.service.mobile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AppBannerResponse {
    private String type;
    private String domain;
    private String path;
    private String thumb;
}
