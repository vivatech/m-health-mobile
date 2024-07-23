package com.service.mobile.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AddRatingRequest {
    private Integer user_id;
    private Integer case_id;
    private Integer doctor_id;
    private Float rating;
    private String comment;
}
