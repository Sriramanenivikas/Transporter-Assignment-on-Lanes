package com.freightfox.lanes.controller;

import com.freightfox.lanes.dto.AssignmentRequest;
import com.freightfox.lanes.dto.AssignmentResponse;
import com.freightfox.lanes.dto.InputRequest;
import com.freightfox.lanes.dto.InputResponse;
import com.freightfox.lanes.service.TransporterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transporters")
@RequiredArgsConstructor
public class TransporterController {

    private final TransporterService transporterService;

    @PostMapping("/input")
    public ResponseEntity<InputResponse> submitInput(@Valid @RequestBody InputRequest request) {
        return ResponseEntity.ok(transporterService.saveInput(request));
    }

    @PostMapping("/assignment")
    public ResponseEntity<AssignmentResponse> getAssignment(@Valid @RequestBody AssignmentRequest request) {
        return ResponseEntity.ok(transporterService.computeAssignment(request.getMaxTransporters()));
    }
}

