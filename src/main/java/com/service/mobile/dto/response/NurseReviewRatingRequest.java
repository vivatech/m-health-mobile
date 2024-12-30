package com.service.mobile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class NurseReviewRatingRequest {
    private Integer user_id;
    private Integer order_id;
    private String comment;
    private String rating;
}
