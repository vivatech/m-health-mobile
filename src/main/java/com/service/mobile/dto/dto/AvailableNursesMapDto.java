package com.service.mobile.dto.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AvailableNursesMapDto {
    private String number;
    private String nurseName;
    private LocalTime time;
    private Float lati;
    private Float longi;
}
