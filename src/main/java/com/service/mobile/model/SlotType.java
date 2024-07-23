package com.service.mobile.model;

import com.service.mobile.dto.enums.SlotStatus;
import com.service.mobile.dto.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "mh_slot_type")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SlotType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String value;

    @Enumerated(EnumType.STRING)
    private SlotStatus status = SlotStatus.inactive;
}
