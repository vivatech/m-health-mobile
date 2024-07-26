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
public class ListSupportTicketsRequest {
    private Integer user_id;
    private SupportTicketStatus status=SupportTicketStatus.Open;
    private String name;
    private Integer id;
    private Integer page;
}
