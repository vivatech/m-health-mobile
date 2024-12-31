package com.service.mobile.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "mh_users_intiated")
public class UserInitiated {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "mobile_number", nullable = false)
    private Integer mobileNumber;

    @Column(name = "promo_code_of")
    private Integer promoCodeOf;

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

}
