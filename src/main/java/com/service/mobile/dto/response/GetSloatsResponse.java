package com.service.mobile.dto.response;

import com.service.mobile.dto.enums.AddedType;
import com.service.mobile.dto.enums.ConsultationType;
import com.service.mobile.dto.enums.RequestType;
import com.service.mobile.model.SlotType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GetSloatsResponse {
    private String slot_day;
    private String slot_time;
    private Integer slot_type;
    private LocalDate consultation_date;
    private Integer to;
    private String name;
    private RequestType status;
    private ConsultationType consultation_type;
    private AddedType added_type;
    private String specialization;
    private String profile_picture;
}
