package com.freightfox.lanes.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LaneQuoteDto {

    @NotNull(message = "Lane id is required")
    private Integer laneId;

    @Positive(message = "Quote must be positive")
    private double quote;
}

