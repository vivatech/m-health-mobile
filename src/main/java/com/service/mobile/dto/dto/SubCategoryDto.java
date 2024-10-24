package com.service.mobile.dto.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SubCategoryDto {
    private String  cat_id;
    private String cat_name;
    private String cat_name_sl;
    private String sub_cat_name_sl;
    private String sub_cat_name;
}
