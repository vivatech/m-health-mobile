package com.service.mobile.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddRatingRequest {
    @NotNull(message = "User Id should not be null")
    private String user_id;

    @NotNull(message = "Case Id should not be null")
    private String case_id;

    @NotNull(message = "Doctor Id should not be null")
    private String doctor_id;

    private String rating;

    @NotNull(message = "Comment message should not be null")
    private String comment;
}
