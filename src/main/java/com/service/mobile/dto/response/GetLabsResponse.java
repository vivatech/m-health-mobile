package com.service.mobile.dto.response;

import com.service.mobile.dto.dto.LabsDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GetLabsResponse {
    private List<LabsDto> labs;
}
