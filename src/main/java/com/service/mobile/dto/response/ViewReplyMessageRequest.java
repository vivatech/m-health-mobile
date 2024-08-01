package com.service.mobile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ViewReplyMessageRequest {
    private Integer user_id;
    private Integer support_ticket_id;
    private Integer page;
}
