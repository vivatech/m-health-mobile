package com.service.mobile.dto.availabiltyDoctorDto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.service.mobile.dto.enums.FeeType;
import lombok.*;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorAvailabilityRequest {
    private String user_id;
    private String doctor_id;
    private LocalDate date;
    private String consult_type;
}
