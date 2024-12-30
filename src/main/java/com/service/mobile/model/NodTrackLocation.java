package com.service.mobile.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "mh_nod_track_location")
public class NodTrackLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "patient_id", nullable = false)
    private int patientId;

    @Column(name = "nurse_id", nullable = false)
    private int nurseId;

    @Column(name = "search_id", nullable = false)
    private String searchId;

    @Column(name = "onway_lat")
    private String onwayLat;

    @Column(name = "onway_lng", length = 50)
    private String onwayLng;

    @Column(name = "arrived_lat", length = 50)
    private String arrivedLat;

    @Column(name = "arrived_lng", length = 50)
    private String arrivedLng;

    @Column(name = "cancel_lat", length = 50)
    private String cancelLat;

    @Column(name = "cancel_lng", length = 50)
    private String cancelLng;

    @Column(name = "complete_lat", length = 50)
    private String completeLat;

    @Column(name = "complete_lng", length = 50)
    private String completeLng;
}
