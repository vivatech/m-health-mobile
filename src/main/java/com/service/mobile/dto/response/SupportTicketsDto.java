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
public class SupportTicketsDto {
    private Integer id;
    private String name;
    private String description;
    private String photo;
    private String attachment_type;
    private SupportTicketStatus status;
    private String created_by;
    private LocalDateTime created_date;
    private Long total_count;
}
