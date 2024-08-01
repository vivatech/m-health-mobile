package com.service.mobile.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateRelativeProfileRequest {
    private Byte id;
    private Integer user_id;
    private String relation_with_patient;
    private String name;
    private LocalDate dob;
    private String status;
    private MultipartFile Profile_picture;
}
