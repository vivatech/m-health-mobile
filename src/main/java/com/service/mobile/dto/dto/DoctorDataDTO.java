package com.service.mobile.dto.dto;

import com.service.mobile.dto.enums.ConsultationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DoctorDataDTO {
    private String first_name;
    private Date consultation_date;
    private String transaction_id;
    private String slot_time;
    private String amount;
    private ConsultationType consultation_type;
    private String profile_photo;
    private NurseDto nurse;
    private ClinicInformation clinic;
    private String consult_type;
    private String package_name;
}
