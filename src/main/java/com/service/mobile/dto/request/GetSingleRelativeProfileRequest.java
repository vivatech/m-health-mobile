package com.service.mobile.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GetSingleRelativeProfileRequest {
    private Integer user_id;
    private Integer case_id;
    private LocalDate newOrderDate;
    private Integer category_id;
    private Integer subcategory_id;
    private String cancel_message;
    private Byte id;
}
