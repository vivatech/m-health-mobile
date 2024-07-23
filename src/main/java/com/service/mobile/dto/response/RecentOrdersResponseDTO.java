package com.service.mobile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RecentOrdersResponseDTO {
    private String status;
    private String message;
    private String firstName;
    private String lastName;
    private String profilePicture;
    private List<ConsultationDTO> data;
}
