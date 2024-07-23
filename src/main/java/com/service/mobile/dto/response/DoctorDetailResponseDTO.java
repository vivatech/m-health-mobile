package com.service.mobile.dto.response;

import com.service.mobile.dto.dto.DoctorDataDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DoctorDetailResponseDTO {
    private String status;
    private String message;
    private DoctorDataDTO data;
}
