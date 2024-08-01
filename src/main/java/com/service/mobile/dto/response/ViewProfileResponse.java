package com.service.mobile.dto.response;

import com.service.mobile.dto.dto.CommentsDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ViewProfileResponse {
    private String first_name;
    private String last_name;
    private String email;
    private String contact_number;
    private String photo;
    private String country;
    private String state;
    private String city;
    private String hospital_address;
    private String residence_address;
    private String professional_identification_number;
    private Double rating;
    private String extra_activities;
    private String about_me;
    private String language;
    private List<CommentsDto> review;
    private String gender;
}
