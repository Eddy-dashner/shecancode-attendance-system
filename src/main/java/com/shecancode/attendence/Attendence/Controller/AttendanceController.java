package com.shecancode.attendence.Attendence.Controller;

import com.shecancode.attendence.Attendence.Service.AttendanceService;
import com.shecancode.attendence.Attendence.dao.AttendanceResponse;
import com.shecancode.attendence.Attendence.dao.BulkAttendanceRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/programs/{programId}/cohorts/{cohortId}/attendance")
@Validated // Ensures @Valid works on nested objects if needed
@Tag(name = "Attendance", description = "Bulk attendance recording and updates (ADMIN or TRAINER)")
public class AttendanceController {
    private final AttendanceService attendanceService;


    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping
    @Operation(summary = "Record bulk attendance (ADMIN or TRAINER)",
            description = "Records attendance for a list of students on a given date. Students already recorded for that " +
                    "date, or not belonging to this program/cohort, are skipped. Returns 204 when nothing new is recorded.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Attendance records created"),
            @ApiResponse(responseCode = "204", description = "Nothing recorded (all duplicates or not in this program/cohort)"),
            @ApiResponse(responseCode = "400", description = "Validation failed (missing date, empty student list, etc.)", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is a STUDENT (not permitted)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Program, cohort, or a student in the payload not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "A student in the payload is DROPPED_OUT", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ADMIN','TRAINER')")
    public ResponseEntity<List<AttendanceResponse>> recordBulkAttendance(
            @PathVariable UUID programId,
            @PathVariable UUID cohortId,
            @Valid @RequestBody BulkAttendanceRequest request) {

        List<AttendanceResponse> responses = attendanceService.recordBulkAttendance(request, programId, cohortId);
        if (responses.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return new ResponseEntity<>(responses, HttpStatus.CREATED);
    }

    @PatchMapping
    @Operation(summary = "Update bulk attendance (ADMIN or TRAINER)",
            description = "Updates existing attendance records for the given date/cohort. Only students with an existing " +
                    "record for that date are updated; others are ignored. Returns 204 when no matching records exist.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Attendance records updated"),
            @ApiResponse(responseCode = "204", description = "No matching records to update"),
            @ApiResponse(responseCode = "400", description = "Validation failed (missing date, empty student list, etc.)", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is a STUDENT (not permitted)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Program not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ADMIN','TRAINER')")
    public ResponseEntity<List<AttendanceResponse>> updateBulkAttendance(
            @PathVariable UUID programId,
            @PathVariable UUID cohortId,
            @Valid @RequestBody BulkAttendanceRequest request) {

        List<AttendanceResponse> responses = attendanceService.updateBulkAttendance(request, programId, cohortId);

        if (responses.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(responses);
    }
}
