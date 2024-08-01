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
public class ReplySupportTicketRequest {
    private Integer user_id;
    private Integer support_ticket_id;
    private String message;
    private String attachment_type ;
    private MultipartFile filename;
}
