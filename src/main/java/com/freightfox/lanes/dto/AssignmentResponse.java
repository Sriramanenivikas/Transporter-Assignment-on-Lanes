package com.freightfox.lanes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentResponse {

    private String status;
    private double totalCost;
    private List<LaneAssignment> assignments;
    private List<Integer> selectedTransporters;
}

