package com.service.mobile.dto.request;

import com.service.mobile.dto.enums.FeeType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DoctorAvailabilityListLatestRequest {
    private Integer user_id;
    private Integer doctor_id;
    private LocalDate date;
    private LocalDate new_order_date;
    private FeeType consult_type;
}
