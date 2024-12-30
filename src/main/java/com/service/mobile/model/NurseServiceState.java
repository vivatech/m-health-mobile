package com.service.mobile.model;

import com.service.mobile.dto.enums.CancelBy;
import com.service.mobile.dto.enums.ConfirmAck;
import com.service.mobile.dto.enums.DeviceEnv;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.service.mobile.dto.enums.State;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "mh_dservice_state")
public class NurseServiceState {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "order_id")
    private Integer orderId;

    @Column(name = "patient_id")
    private Integer patientId;

    @Column(name = "nurse_id")
    private Integer nurseId;

    @Column(name = "lat_patient")
    private String latPatient;

    @Column(name = "long_patient")
    private String longPatient;

    @Column(name = "lat_nurse")
    private String latNurse;

    @Column(name = "long_nurse")
    private String longNurse;

    @Enumerated(EnumType.STRING)
    private State state;

    private String distance;

    @Column(name = "search_id")
    private String searchId;

    @Lob
    @Column(name = "p_remark")
    private String pRemark;

    @Column(name = "p_rating")
    private String pRating;

    @Column(name = "rating_notified_to_patient")
    private int ratingNotifiedToPatient;

    @Lob
    @Column(name = "n_remark")
    private String nRemark;

    @Column(name = "n_rating")
    private String nRating;

    @Lob
    @Column(name = "cancel_message")
    private String cancelMessage;

    @Enumerated(EnumType.STRING)
    @Column(name = "cancel_by")
    private CancelBy cancelBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_env")
    private DeviceEnv deviceEnv;

    @Column(name = "confirm_ack")
    private String confirmAck;

    @Column(name = "cancel_at")
    private LocalDateTime cancelAt;

    @Column(name = "arrived_at")
    private LocalDateTime arrivedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
