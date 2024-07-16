package com.service.mobile.dto.response;

import com.service.mobile.dto.enums.DeviceType;
import com.service.mobile.dto.enums.UserType;
import com.service.mobile.model.MobileRelease;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MobileReleaseDto {

    private Integer id;
    private String app_version;
    private String client_name;
    private Integer is_depricated;
    private Integer is_terminated;
    private String message;
    private UserType user_type;
    private DeviceType device_type;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    public MobileReleaseDto(MobileRelease release){
        this.id = release.getId();
        this.app_version = release.getAppVersion();
        this.client_name = release.getClientName();
        this.is_depricated = (release.getIsDeprecated()!=null && release.getIsDeprecated())?1:0;
        this.is_terminated = (release.getIsTerminated()!=null && release.getIsTerminated())?1:0;
        this.message = release.getMessage();
        this.user_type = release.getUserType();
        this.device_type = release.getDeviceType();
        this.created_at = release.getCreatedAt();
        this.updated_at = release.getUpdatedAt();
    }
}
