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
    private	String user_id;
    private String fullName;
    private String email;
    private String gender;
    private String residence_address;
    private String city_id;
    private String dob;
    private String contact_number;
    private String country_id;
    private String state_id;
}
