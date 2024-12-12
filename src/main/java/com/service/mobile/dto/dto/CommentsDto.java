package com.service.mobile.dto.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CommentsDto {
    private String comment;
    private String name;
    private Integer rating;
    private String created_at;
    private String file_url;
    private Long total_count;
}
