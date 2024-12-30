package com.service.mobile.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "mh_sms_log")
public class SmsLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private int logId;

    @Column(name = "from_id", nullable = false)
    private int fromId;

    @Column(name = "to_id", nullable = false)
    private int toId;

    @Column(name = "sms_for", nullable = false, length = 50)
    private String smsFor;

    @Column(name = "msg", columnDefinition = "TEXT")
    private String msg;

    @Column(name = "case_id")
    private Integer caseId;

    @Column(name = "consult_date")
    private LocalDateTime consultDate;

    @Column(name = "is_sent", nullable = false)
    private Integer isSent;

    @Column(name = "user_type", length = 20)
    private String userType;

    @Column(name = "lab_orders_id")
    private Integer labOrdersId;
}

