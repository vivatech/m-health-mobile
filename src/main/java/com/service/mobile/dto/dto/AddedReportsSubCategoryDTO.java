package com.service.mobile.dto.dto;

import com.service.mobile.dto.enums.CategoryStatus;
import com.service.mobile.dto.enums.YesNo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AddedReportsSubCategoryDTO {
    private Integer sub_cat_id;
    private Integer cat_id;
    private String sub_cat_name;
    private String sub_cat_name_sl;
    private CategoryStatus sub_cat_status;
    private YesNo is_home_consultant_available;
    private LocalDateTime sub_cat_created_at;
    private LocalDateTime sub_cat_updated_at;
}
