package com.service.mobile.dto.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClinicInformation {
    private String name;
    private String contact_number;
    private String address;
    private Float latitude;
    private Float longitude;
    private String profile_picture;
}
