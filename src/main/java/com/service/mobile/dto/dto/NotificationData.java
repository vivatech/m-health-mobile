package com.service.mobile.dto.dto;

import com.service.mobile.dto.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationData {
    private Integer fromId;
    private Integer toId;
    private Integer caseId;
    private String url;
    private NotificationType type;
}
