package com.service.mobile.model;

import com.service.mobile.dto.enums.UserType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "mh_authkey")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "auth_key")
    private String authKey;

    @Column(name = "device_token")
    private String deviceToken;

    @Column(name = "login_type")
    @Enumerated(EnumType.STRING)
    private UserType loginType;

    @Column(name = "created_date")
    private LocalDateTime createdDate;
}

