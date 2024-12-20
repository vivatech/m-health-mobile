package com.service.mobile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class VideoAttachmentResponse {
    private Integer id;
    private Integer from_id;
    private Integer to_id;
    private String url;
    private Long created_at;
    private String url_type;
}
