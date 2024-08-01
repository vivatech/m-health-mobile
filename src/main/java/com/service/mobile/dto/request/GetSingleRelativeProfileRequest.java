package com.service.mobile.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GetSingleRelativeProfileRequest {
    private Integer user_id;
    private Integer case_id;
    private Integer category_id;
    private Integer subcategory_id;
    private Byte id;
}
