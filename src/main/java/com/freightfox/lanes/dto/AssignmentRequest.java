package com.freightfox.lanes.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentRequest {

    @Min(value = 1, message = "maxTransporters must be at least 1")
    private int maxTransporters;
}

