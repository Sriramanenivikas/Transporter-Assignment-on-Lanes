package com.freightfox.lanes.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InputRequest {

    @NotEmpty(message = "Lanes list is required")
    @Valid
    private List<LaneDto> lanes;

    @NotEmpty(message = "Transporters list is required")
    @Valid
    private List<TransporterDto> transporters;
}

