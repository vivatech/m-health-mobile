package com.service.mobile.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EditProfileRequest {
    private	Integer user_id;
    private String fullName;
    private String email;
    private String gender;
    private String residence_address;
    private Integer city_id;
    private LocalDate dob;
}
