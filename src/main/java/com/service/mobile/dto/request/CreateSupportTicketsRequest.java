package com.service.mobile.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateSupportTicketsRequest {
    private Integer user_id;
    private String support_ticket_title;
    private String support_ticket_description;
    private String attachment_type;
    private MultipartFile filename ;
}
