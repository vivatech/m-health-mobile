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
    private String user_id;
    private String doctor_id;
    private String case_id;
    private LocalDate newOrderDate;
    private String category_id;
    private String subcategory_id;
    private String cancel_message;
    private Byte id;
}
