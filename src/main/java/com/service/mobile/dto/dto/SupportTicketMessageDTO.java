package com.service.mobile.dto.dto;

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
public class SupportTicketMessageDTO {
    private Integer id;
    private String message;
    private String ticket_name;
    private SupportTicketStatus status = SupportTicketStatus.Open;
    private String attachment;
    private String attachment_type;
    private String created_by;
    private Integer created_by_id;
    private String  created_date;
    private Long total_count;
}
