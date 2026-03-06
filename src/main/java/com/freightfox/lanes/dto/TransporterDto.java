package com.freightfox.lanes.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransporterDto {

    @NotNull(message = "Transporter id is required")
    private Integer id;

    @NotBlank(message = "Transporter name is required")
    private String name;

    @NotEmpty(message = "Lane quotes are required")
    @Valid
    private List<LaneQuoteDto> laneQuotes;
}

