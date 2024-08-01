package com.service.mobile.dto.response;

import com.service.mobile.dto.enums.SupportTicketStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SupportTicketReplyResponseDTO {
    private Integer id;
    private String message;
    private String ticketName;
    private SupportTicketStatus status = SupportTicketStatus.Open;
    private String attachment;
    private String createdBy;
    private Integer createdById;
    private LocalDateTime createdDate;
}
