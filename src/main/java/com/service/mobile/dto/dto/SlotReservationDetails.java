package com.service.mobile.dto.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SlotReservationDetails {
    private int number_slots_to_allocated;
    private String slot_time;
    private String display_time;
    private String is_nurse_avaliabe;
    private int allocated_nurse;
    private List<Integer> allocated_slots;
}
