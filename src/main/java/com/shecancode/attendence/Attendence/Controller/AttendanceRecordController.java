package com.shecancode.attendence.Attendence.Controller;

import com.shecancode.attendence.Attendence.Enum.AlertStatus;
import com.shecancode.attendence.Attendence.Service.AttendanceService;
import com.shecancode.attendence.Attendence.dao.AttendanceAlertResponse;
import com.shecancode.attendence.Attendence.dao.AttendanceEntryRequest;
import com.shecancode.attendence.Attendence.dao.StudentAttendanceSaveResponse;
import com.shecancode.attendence.auth.model.AppUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** Attendance endpoints addressed by id rather than by program/cohort. */
@RestController
@RequestMapping("/api/v1/attendance")
@Tag(name = "Attendance")
public class AttendanceRecordController {
    private final AttendanceService attendanceService;

    public AttendanceRecordController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PatchMapping("/{attendanceId}")
    @Operation(summary = "Update one attendance record (ADMIN or TRAINER)",
            description = "Partial update: only the fields sent are changed. Trainers can only change records dated " +
                    "today; admins can change any record. Rescores the student and raises or resolves alerts.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Attendance updated"),
            @ApiResponse(responseCode = "400", description = "Validation failed (e.g. PRESENT without a check-in time)", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is a STUDENT, or a TRAINER changing a record not dated today", content = @Content),
            @ApiResponse(responseCode = "404", description = "Attendance record not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Student has dropped out, or a concurrent save; retry", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ADMIN','TRAINER')")
    public ResponseEntity<StudentAttendanceSaveResponse> updateAttendance(
            @PathVariable UUID attendanceId,
            @Valid @RequestBody AttendanceEntryRequest request,
            @AuthenticationPrincipal AppUser caller) {

        return ResponseEntity.ok(attendanceService.updateAttendance(attendanceId, request, caller));
    }

    @GetMapping("/alerts")
    @Operation(summary = "List absence alerts (ADMIN or TRAINER)",
            description = "Alerts raised when a student reaches 3 absences in a row or 3 in total. " +
                    "Defaults to ACTIVE alerts across all cohorts; filter with cohortId and status.")
    @PreAuthorize("hasAnyRole('ADMIN','TRAINER')")
    public ResponseEntity<List<AttendanceAlertResponse>> getAlerts(
            @RequestParam(required = false) UUID cohortId,
            @RequestParam(defaultValue = "ACTIVE") AlertStatus status) {

        return ResponseEntity.ok(attendanceService.getAlerts(cohortId, status));
    }
}
