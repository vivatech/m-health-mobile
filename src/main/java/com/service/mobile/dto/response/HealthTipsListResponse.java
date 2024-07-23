package com.service.mobile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class HealthTipsListResponse {
    private Integer package_id;
    private String name;
    private Boolean is_video;
    private String video;
    private String video_thumb;
    private String description;
    private String description_formated;
    private String photo;
    private String category_name;
    private Integer category_id;
    private String status;
    private Integer total_count;
}
