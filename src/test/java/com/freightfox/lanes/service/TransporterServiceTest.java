package com.freightfox.lanes.service;

import com.freightfox.lanes.dto.*;
import com.freightfox.lanes.exception.DataNotLoadedException;
import com.freightfox.lanes.exception.InvalidInputException;
import com.freightfox.lanes.exception.NoFeasibleAssignmentException;
import com.freightfox.lanes.repository.TransporterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

class TransporterServiceTest {

    private TransporterService service;
    private TransporterRepository repository;

    @BeforeEach
    void setUp() {
        repository = new TransporterRepository();
        service = new TransporterService(repository);
    }


    private InputRequest buildTestCase1() {
        List<LaneDto> lanes = List.of(
                new LaneDto(1, "Mumbai", "Delhi"),
                new LaneDto(2, "Delhi", "Bangalore"),
                new LaneDto(3, "Chennai", "Kolkata"),
                new LaneDto(4, "Pune", "Hyderabad"),
                new LaneDto(5, "Ahmedabad", "Jaipur")
        );

        List<TransporterDto> transporters = List.of(
                new TransporterDto(1, "T1", List.of(
                        new LaneQuoteDto(1, 20835), new LaneQuoteDto(2, 10512),
                        new LaneQuoteDto(3, 22105), new LaneQuoteDto(4, 42481),
                        new LaneQuoteDto(5, 19862))),
                new TransporterDto(2, "T2", List.of(
                        new LaneQuoteDto(1, 48844), new LaneQuoteDto(2, 31326),
                        new LaneQuoteDto(3, 18640), new LaneQuoteDto(4, 45828),
                        new LaneQuoteDto(5, 18297))),
                new TransporterDto(3, "T3", List.of(
                        new LaneQuoteDto(1, 39020), new LaneQuoteDto(2, 20648),
                        new LaneQuoteDto(3, 31438), new LaneQuoteDto(4, 36447),
                        new LaneQuoteDto(5, 12789))),
                new TransporterDto(4, "T4", List.of(
                        new LaneQuoteDto(1, 14400), new LaneQuoteDto(2, 44514),
                        new LaneQuoteDto(3, 14316), new LaneQuoteDto(4, 10678),
                        new LaneQuoteDto(5, 13032))),
                new TransporterDto(5, "T5", List.of(
                        new LaneQuoteDto(1, 11601), new LaneQuoteDto(2, 19760),
                        new LaneQuoteDto(3, 40870), new LaneQuoteDto(4, 20635),
                        new LaneQuoteDto(5, 26421))),
                new TransporterDto(6, "T6", List.of(
                        new LaneQuoteDto(1, 35095), new LaneQuoteDto(2, 12494),
                        new LaneQuoteDto(3, 17808), new LaneQuoteDto(4, 36210),
                        new LaneQuoteDto(5, 39444))),
                new TransporterDto(7, "T7", List.of(
                        new LaneQuoteDto(1, 26070), new LaneQuoteDto(2, 41098),
                        new LaneQuoteDto(3, 20932), new LaneQuoteDto(4, 16897),
                        new LaneQuoteDto(5, 27938)))
        );

        return new InputRequest(lanes, transporters);
    }

    private InputRequest buildSimpleTestCase() {
        List<LaneDto> lanes = List.of(
                new LaneDto(1, "A", "B"),
                new LaneDto(2, "C", "D")
        );

        List<TransporterDto> transporters = List.of(
                new TransporterDto(1, "T1", List.of(
                        new LaneQuoteDto(1, 100), new LaneQuoteDto(2, 200))),
                new TransporterDto(2, "T2", List.of(
                        new LaneQuoteDto(1, 150), new LaneQuoteDto(2, 100)))
        );

        return new InputRequest(lanes, transporters);
    }


    @Test
    @DisplayName("saveInput — valid input saves successfully")
    void testSaveInput_Success() {
        InputResponse resp = service.saveInput(buildSimpleTestCase());

        assertThat(resp.getStatus()).isEqualTo("success");
        assertThat(repository.isDataLoaded()).isTrue();
        assertThat(repository.getLanes()).hasSize(2);
        assertThat(repository.getTransporters()).hasSize(2);
    }

    @Test
    @DisplayName("saveInput — duplicate lane ids rejected")
    void testSaveInput_DuplicateLane() {
        InputRequest req = new InputRequest(
                List.of(new LaneDto(1, "A", "B"), new LaneDto(1, "C", "D")),
                List.of(new TransporterDto(1, "T1", List.of(new LaneQuoteDto(1, 100))))
        );

        assertThatThrownBy(() -> service.saveInput(req))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("Duplicate lane id");
    }

    @Test
    @DisplayName("saveInput — transporter quoting non-existent lane rejected")
    void testSaveInput_InvalidLaneRef() {
        InputRequest req = new InputRequest(
                List.of(new LaneDto(1, "A", "B")),
                List.of(new TransporterDto(1, "T1", List.of(new LaneQuoteDto(99, 100))))
        );

        assertThatThrownBy(() -> service.saveInput(req))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("non-existent lane");
    }

    @Test
    @DisplayName("saveInput — duplicate transporter ids rejected")
    void testSaveInput_DuplicateTransporter() {
        InputRequest req = new InputRequest(
                List.of(new LaneDto(1, "A", "B")),
                List.of(
                        new TransporterDto(1, "T1", List.of(new LaneQuoteDto(1, 100))),
                        new TransporterDto(1, "T1-dup", List.of(new LaneQuoteDto(1, 200)))
                )
        );

        assertThatThrownBy(() -> service.saveInput(req))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("Duplicate transporter id");
    }

    @Test
    @DisplayName("saveInput — duplicate lane quote within transporter rejected")
    void testSaveInput_DuplicateLaneQuoteInTransporter() {
        InputRequest req = new InputRequest(
                List.of(new LaneDto(1, "A", "B"), new LaneDto(2, "C", "D")),
                List.of(new TransporterDto(1, "T1", List.of(
                        new LaneQuoteDto(1, 100),
                        new LaneQuoteDto(1, 110),
                        new LaneQuoteDto(2, 200))))
        );

        assertThatThrownBy(() -> service.saveInput(req))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("duplicate quote for lane");
    }


    @Test
    @DisplayName("computeAssignment — throws when no data loaded")
    void testAssignment_NoData() {
        assertThatThrownBy(() -> service.computeAssignment(3))
                .isInstanceOf(DataNotLoadedException.class);
    }

    @Test
    @DisplayName("computeAssignment — simple case picks cheapest combination")
    void testAssignment_Simple() {
        service.saveInput(buildSimpleTestCase());

        AssignmentResponse resp = service.computeAssignment(2);

        assertThat(resp.getStatus()).isEqualTo("success");
        assertThat(resp.getTotalCost()).isEqualTo(200.0);
        assertThat(resp.getAssignments()).hasSize(2);
    }

    @Test
    @DisplayName("computeAssignment — single transporter covers all lanes")
    void testAssignment_SingleTransporter() {
        service.saveInput(buildSimpleTestCase());

        AssignmentResponse resp = service.computeAssignment(1);

        assertThat(resp.getStatus()).isEqualTo("success");
        assertThat(resp.getTotalCost()).isEqualTo(250.0);
        assertThat(resp.getSelectedTransporters()).containsExactly(2);
    }

    @Test
    @DisplayName("computeAssignment — all lanes covered")
    void testAssignment_AllLanesCovered() {
        service.saveInput(buildTestCase1());

        AssignmentResponse resp = service.computeAssignment(3);

        Set<Integer> assignedLanes = resp.getAssignments().stream()
                .map(LaneAssignment::getLaneId)
                .collect(Collectors.toSet());

        assertThat(assignedLanes).containsExactlyInAnyOrder(1, 2, 3, 4, 5);
        assertThat(resp.getSelectedTransporters()).hasSizeLessThanOrEqualTo(3);
    }

    @Test
    @DisplayName("computeAssignment — maxTransporters > available uses all if needed")
    void testAssignment_MaxGreaterThanAvailable() {
        service.saveInput(buildSimpleTestCase());

        AssignmentResponse resp = service.computeAssignment(10);

        assertThat(resp.getStatus()).isEqualTo("success");
        assertThat(resp.getTotalCost()).isEqualTo(200.0);
    }

    @Test
    @DisplayName("computeAssignment — no feasible assignment throws")
    void testAssignment_NoFeasible() {
        InputRequest req = new InputRequest(
                List.of(new LaneDto(1, "A", "B"), new LaneDto(2, "C", "D")),
                List.of(
                        new TransporterDto(1, "T1", List.of(new LaneQuoteDto(1, 100))),
                        new TransporterDto(2, "T2", List.of(new LaneQuoteDto(1, 200)))
                )
        );
        service.saveInput(req);

        assertThatThrownBy(() -> service.computeAssignment(2))
                .isInstanceOf(NoFeasibleAssignmentException.class)
                .hasMessageContaining("No feasible assignment");
    }

    @Test
    @DisplayName("computeAssignment — test case 1 from assignment (max 3 transporters)")
    void testAssignment_TestCase1_Max3() {
        service.saveInput(buildTestCase1());

        AssignmentResponse resp = service.computeAssignment(3);

        assertThat(resp.getStatus()).isEqualTo("success");
        assertThat(resp.getAssignments()).hasSize(5);
        assertThat(resp.getTotalCost()).isEqualTo(60139.0);
        assertThat(resp.getSelectedTransporters()).containsExactly(1, 4, 5);
        assertThat(resp.getAssignments())
                .extracting(LaneAssignment::getLaneId, LaneAssignment::getTransporterId)
                .containsExactlyInAnyOrder(
                        tuple(1, 5),
                        tuple(2, 1),
                        tuple(3, 4),
                        tuple(4, 4),
                        tuple(5, 4));
    }
}
