package com.shecancode.attendence.Attendence.Controller;

import com.shecancode.attendence.Attendence.Service.AttendanceService;
import com.shecancode.attendence.Attendence.dto.AttendanceResponse;
import com.shecancode.attendence.Attendence.dto.BulkAttendanceRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/programs/{programId}/cohorts/{cohortId}/attendance")
@Validated // Ensures @Valid works on nested objects if needed
public class AttendanceController {
    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping
    public ResponseEntity<List<AttendanceResponse>> recordBulkAttendance(
            @PathVariable UUID programId,
            @PathVariable UUID cohortId,
            @Valid @RequestBody BulkAttendanceRequest request) {

        List<AttendanceResponse> responses = attendanceService.recordBulkAttendance(request, programId, cohortId);

        // If the service skipped all records (e.g., all were duplicates),
        // you might return 200 OK or 204 No Content instead of 201 Created.
        if (responses.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return new ResponseEntity<>(responses, HttpStatus.CREATED);
    }
}
