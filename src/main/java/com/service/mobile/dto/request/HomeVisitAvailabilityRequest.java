package com.service.mobile.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HomeVisitAvailabilityRequest {
    private Integer user_id;
    private Integer slot_id;
    private Integer doctor_id;
    private LocalDate consultation_date;
}
