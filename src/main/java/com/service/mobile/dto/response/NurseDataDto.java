package com.service.mobile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class NurseDataDto {
    private Integer id;
    private String name;
    private String contact_number;
    private String profile_picture;
    private float rating;
}
