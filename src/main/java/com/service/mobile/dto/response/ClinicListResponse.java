package com.service.mobile.dto.response;

import com.service.mobile.dto.dto.LocationDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ClinicListResponse {
    private Integer clinic_id;
    private String clinic_name;
    private String image;
    private String address;
    private LocationDto location;
    private String location_image;
}
