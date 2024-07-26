package com.service.mobile.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AddReportRequest {
    private Integer user_id;
    private Integer sub_cat_id;
    private Integer category_id;
}
