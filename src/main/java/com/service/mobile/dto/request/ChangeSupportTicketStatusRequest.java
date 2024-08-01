package com.service.mobile.dto.request;

import com.service.mobile.dto.enums.SupportTicketStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ChangeSupportTicketStatusRequest {
    private Integer user_id;
    private Integer support_ticket_id;
    private SupportTicketStatus status;
}
