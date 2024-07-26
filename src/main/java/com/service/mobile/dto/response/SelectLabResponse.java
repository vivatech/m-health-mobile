package com.service.mobile.dto.response;

import com.service.mobile.dto.dto.ConsultDetailSummaryDto;
import com.service.mobile.dto.dto.LabsDto;
import com.service.mobile.dto.dto.ReportSubCatDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SelectLabResponse {
    private List<LabsDto> labs;
    private List<ReportSubCatDto> reports;
    private ConsultDetailSummaryDto summary;
}
