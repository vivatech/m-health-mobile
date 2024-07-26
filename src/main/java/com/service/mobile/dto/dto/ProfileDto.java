package com.service.mobile.dto.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProfileDto {
    public String first_name;
    public String last_name;
    public String fullName;
    public String email;
    public String contact_number;
    public String photo;
    public String country;
    public String country_code;
    public String state;
    public String city;
    public String residence_address;
    public String address;
    public LocalDate dob;
    public String gender;
}
