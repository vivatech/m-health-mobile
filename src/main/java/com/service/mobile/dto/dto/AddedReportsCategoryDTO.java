package com.service.mobile.dto.dto;

import com.service.mobile.dto.enums.CategoryStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AddedReportsCategoryDTO {
    private Integer cat_id;
    private String cat_name;
    private String cat_name_sl;
    private CategoryStatus cat_status;
    private LocalDateTime cat_created_at;
    private LocalDateTime cat_updated_at;
}
