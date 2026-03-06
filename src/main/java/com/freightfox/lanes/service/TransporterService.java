package com.freightfox.lanes.service;

import com.freightfox.lanes.dto.AssignmentResponse;
import com.freightfox.lanes.dto.InputRequest;
import com.freightfox.lanes.dto.InputResponse;
import com.freightfox.lanes.dto.LaneAssignment;
import com.freightfox.lanes.dto.LaneDto;
import com.freightfox.lanes.dto.LaneQuoteDto;
import com.freightfox.lanes.dto.TransporterDto;
import com.freightfox.lanes.exception.DataNotLoadedException;
import com.freightfox.lanes.exception.InvalidInputException;
import com.freightfox.lanes.exception.NoFeasibleAssignmentException;
import com.freightfox.lanes.repository.TransporterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransporterService {

    private final TransporterRepository repository;

    public InputResponse saveInput(InputRequest request) {
        validateInput(request);
        repository.saveAll(request.getLanes(), request.getTransporters());
        return new InputResponse("success", "Input data saved successfully.");
    }

    public AssignmentResponse computeAssignment(int maxTransporters) {
        if (!repository.isDataLoaded()) {
            throw new DataNotLoadedException("No input data loaded. Please call /api/v1/transporters/input first.");
        }

        List<LaneDto> lanes = repository.getLanes();
        List<TransporterDto> transporters = repository.getTransporters();

        Set<Integer> allLaneIds = lanes.stream()
                .map(LaneDto::getId)
                .collect(Collectors.toSet());

        int cap = Math.min(maxTransporters, transporters.size());

        log.info("Computing assignment: {} lanes, {} transporters, max {} to use",
                lanes.size(), transporters.size(), cap);

        Map<Integer, Map<Integer, Double>> transporterQuotes = new HashMap<>();
        for (TransporterDto t : transporters) {
            Map<Integer, Double> quoteMap = new HashMap<>();
            for (LaneQuoteDto lq : t.getLaneQuotes()) {
                quoteMap.put(lq.getLaneId(), lq.getQuote());
            }
            transporterQuotes.put(t.getId(), quoteMap);
        }

        double bestCost = Double.MAX_VALUE;
        List<Integer> bestSubset = null;

        List<Integer> transporterIds = transporters.stream()
                .map(TransporterDto::getId)
                .toList();

        for (int size = 1; size <= cap; size++) {
            List<List<Integer>> combos = combinations(transporterIds, size);
            for (List<Integer> combo : combos) {
                Set<Integer> covered = new HashSet<>();
                for (int tid : combo) {
                    covered.addAll(transporterQuotes.get(tid).keySet());
                }

                if (!covered.containsAll(allLaneIds)) {
                    continue;
                }

                double totalCost = 0;
                boolean feasible = true;
                for (int laneId : allLaneIds) {
                    double minQuote = Double.MAX_VALUE;
                    for (int tid : combo) {
                        Double q = transporterQuotes.get(tid).get(laneId);
                        if (q != null && q < minQuote) {
                            minQuote = q;
                        }
                    }
                    if (minQuote == Double.MAX_VALUE) {
                        feasible = false;
                        break;
                    }
                    totalCost += minQuote;
                }

                if (!feasible)
                    continue;

                if (totalCost < bestCost
                        || (totalCost == bestCost && bestSubset != null && combo.size() > bestSubset.size())) {
                    bestCost = totalCost;
                    bestSubset = combo;
                }
            }
        }

        if (bestSubset == null) {
            throw new NoFeasibleAssignmentException(
                    "No feasible assignment found. No combination of up to " + maxTransporters
                            + " transporters can cover all " + allLaneIds.size() + " lanes.");
        }

        List<LaneAssignment> assignments = new ArrayList<>();
        final List<Integer> selectedSet = bestSubset;

        for (int laneId : allLaneIds.stream().sorted().toList()) {
            int bestTid = -1;
            double bestQuote = Double.MAX_VALUE;
            for (int tid : selectedSet) {
                Double q = transporterQuotes.get(tid).get(laneId);
                if (q != null && q < bestQuote) {
                    bestQuote = q;
                    bestTid = tid;
                }
            }
            assignments.add(LaneAssignment.builder()
                    .laneId(laneId)
                    .transporterId(bestTid)
                    .build());
        }

        List<Integer> sorted = bestSubset.stream().sorted().toList();

        log.info("Optimal assignment: {} transporters, total cost: {}", sorted.size(), bestCost);

        return AssignmentResponse.builder()
                .status("success")
                .totalCost(bestCost)
                .assignments(assignments)
                .selectedTransporters(sorted)
                .build();
    }

    private void validateInput(InputRequest request) {
        Set<Integer> laneIds = new HashSet<>();
        for (LaneDto lane : request.getLanes()) {
            if (!laneIds.add(lane.getId())) {
                throw new InvalidInputException("Duplicate lane id: " + lane.getId());
            }
        }

        Set<Integer> transporterIds = new HashSet<>();
        for (TransporterDto t : request.getTransporters()) {
            if (!transporterIds.add(t.getId())) {
                throw new InvalidInputException("Duplicate transporter id: " + t.getId());
            }
            Set<Integer> quotedLaneIds = new HashSet<>();
            for (LaneQuoteDto lq : t.getLaneQuotes()) {
                if (!quotedLaneIds.add(lq.getLaneId())) {
                    throw new InvalidInputException(
                            "Transporter " + t.getId() + " has duplicate quote for lane: " + lq.getLaneId());
                }
                if (!laneIds.contains(lq.getLaneId())) {
                    throw new InvalidInputException(
                            "Transporter " + t.getId() + " quotes for non-existent lane: " + lq.getLaneId());
                }
            }
        }
    }

    private <T> List<List<T>> combinations(List<T> elements, int k) {
        List<List<T>> result = new ArrayList<>();
        combinationsHelper(elements, k, 0, new ArrayList<>(), result);
        return result;
    }

    private <T> void combinationsHelper(List<T> elements, int k, int start,
            List<T> current, List<List<T>> result) {
        if (current.size() == k) {
            result.add(new ArrayList<>(current));
            return;
        }
        for (int i = start; i < elements.size(); i++) {
            current.add(elements.get(i));
            combinationsHelper(elements, k, i + 1, current, result);
            current.removeLast();
        }
    }
}
