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

    @Column(name = "sortname", nullable = false)
    private String sortname;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "phonecode", nullable = false)
    private Integer phonecode;

    @Column(name = "countries_name")
    private String countriesName;

    @Column(name = "phone_code")
    private Integer phoneCode;

    @Column(name = "sort_name")
    private String sortName;
}

