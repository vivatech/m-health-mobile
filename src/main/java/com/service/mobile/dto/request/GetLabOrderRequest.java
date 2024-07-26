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
public class GetLabOrderRequest {
    private Integer user_id;
    private LocalDate from_date;
    private LocalDate to_date;
    private Integer page;
}
