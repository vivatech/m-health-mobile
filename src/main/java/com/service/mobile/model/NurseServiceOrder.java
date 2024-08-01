package com.service.mobile.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "mh_nurse_service_order")
public class NurseServiceOrder {
    @EmbeddedId
    private NurseServiceOrderKey id;

    @Column(name = "patient_id", nullable = false)
    private Integer patientId;

    @Column(name = "nurse_id", nullable = false)
    private Integer nurseId;
}
