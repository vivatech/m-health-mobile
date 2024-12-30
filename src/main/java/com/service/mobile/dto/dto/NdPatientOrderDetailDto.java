package com.service.mobile.dto.dto;

import com.service.mobile.dto.enums.State;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class NdPatientOrderDetailDto {
    private Integer order_id;
    private String trip_id;
    private String nurse_name;
    private String nurse_contact;
    private String date;
    private String order_amount;
    private State state;
    private String service_type;
}
