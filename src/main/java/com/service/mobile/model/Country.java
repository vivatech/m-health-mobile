package com.service.mobile.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mh_countries")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Country {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "sortname")
    private String sortName;

    @Column(name = "name")
    private String name;

    @Column(name = "phonecode")
    private Integer phonecode;
}

