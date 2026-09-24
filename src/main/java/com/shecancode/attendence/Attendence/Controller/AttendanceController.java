package com.shecancode.attendence.Attendence.Controller;

import com.shecancode.attendence.Attendence.Service.AttendanceService;
import com.shecancode.attendence.Attendence.Enum.AttendanceStatus;
import com.shecancode.attendence.Attendence.dao.AttendanceEntryRequest;
import com.shecancode.attendence.Attendence.dao.AttendanceReportResponse;
import com.shecancode.attendence.Attendence.dao.AttendanceRegisterRequest;
import com.shecancode.attendence.Attendence.dao.AttendanceRegisterResponse;
import com.shecancode.attendence.Attendence.dao.CohortAttendanceSummaryResponse;
import com.shecancode.attendence.Attendence.dao.CohortRegisterResponse;
import com.shecancode.attendence.Attendence.dao.StudentAttendanceHistoryResponse;
import com.shecancode.attendence.Attendence.dao.StudentAttendanceSaveResponse;
import com.shecancode.attendence.auth.model.AppUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/programs/{programId}/cohorts/{cohortId}/attendance")
@Validated
@Tag(name = "Attendance")
public class AttendanceController {
    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PutMapping("/sessions/{date}")
    @Operation(summary = "Save the register for a date (ADMIN or TRAINER)",
            description = "Creates or updates attendance for the listed students on this date. Safe to call repeatedly. " +
                    "Trainers can only save today's register; admins can save any date. Students who are unknown, " +
                    "in another cohort or dropped out are returned in `skipped`. Alerts raised by this save are " +
                    "returned in `alertsRaised` and emailed to the student and recorder. Refused (409) while the " +
                    "cohort or its program is closed.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Register saved"),
            @ApiResponse(responseCode = "400", description = "Validation failed (future date, outside cohort dates, " +
                    "missing check-in time, duplicate student, cohort not in program)", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is a STUDENT, or a TRAINER saving a day other than today", content = @Content),
            @ApiResponse(responseCode = "404", description = "Cohort not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Cohort is closed, or someone else saved this register at the same moment", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ADMIN','TRAINER')")
    public ResponseEntity<AttendanceRegisterResponse> saveRegister(
            @PathVariable UUID programId,
            @PathVariable UUID cohortId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Valid @RequestBody AttendanceRegisterRequest request,
            @AuthenticationPrincipal AppUser caller) {

        return ResponseEntity.ok(attendanceService.saveRegister(programId, cohortId, date, request, caller));
    }

    @PutMapping("/sessions/{date}/students/{studentId}")
    @Operation(summary = "Save one student's attendance for a date (ADMIN or TRAINER)",
            description = "Records or re-records a single student's attendance. Same rules as the full register " +
                    "(trainers: today only), but a student who is unknown, in another cohort or dropped out is an error.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Attendance saved"),
            @ApiResponse(responseCode = "400", description = "Validation failed (missing status, missing check-in time, " +
                    "future date, outside cohort dates, cohort not in program)", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is a STUDENT, or a TRAINER saving a day other than today", content = @Content),
            @ApiResponse(responseCode = "404", description = "Cohort or student not found, or student not in this cohort", content = @Content),
            @ApiResponse(responseCode = "409", description = "Student has dropped out, or a concurrent save; retry", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ADMIN','TRAINER')")
    public ResponseEntity<StudentAttendanceSaveResponse> saveStudentAttendance(
            @PathVariable UUID programId,
            @PathVariable UUID cohortId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PathVariable UUID studentId,
            @Valid @RequestBody AttendanceEntryRequest request,
            @AuthenticationPrincipal AppUser caller) {

        return ResponseEntity.ok(attendanceService.saveStudentAttendance(programId, cohortId, date, studentId, request, caller));
    }

    @GetMapping("/sessions/{date}")
    @Operation(summary = "Get the register for a date (ADMIN or TRAINER)",
            description = "Lists every enrolled student with their status for the date; students not marked yet have a null status.")
    @PreAuthorize("hasAnyRole('ADMIN','TRAINER')")
    public ResponseEntity<CohortRegisterResponse> getRegister(
            @PathVariable UUID programId,
            @PathVariable UUID cohortId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        return ResponseEntity.ok(attendanceService.getRegister(programId, cohortId, date));
    }

    @GetMapping
    @Operation(summary = "Cohort attendance for a date range (ADMIN or TRAINER)",
            description = "Records newest first with totals. from/to default to the last 30 days ending today; " +
                    "a range can cover at most 366 days. Optionally filter by studentId and status.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Report returned"),
            @ApiResponse(responseCode = "400", description = "from after to, range too long, or cohort not in program", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is a STUDENT", content = @Content),
            @ApiResponse(responseCode = "404", description = "Cohort not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ADMIN','TRAINER')")
    public ResponseEntity<AttendanceReportResponse> getCohortAttendance(
            @PathVariable UUID programId,
            @PathVariable UUID cohortId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) UUID studentId,
            @RequestParam(required = false) AttendanceStatus status) {

        return ResponseEntity.ok(attendanceService.getCohortAttendance(programId, cohortId, from, to, studentId, status));
    }

    @GetMapping("/summary")
    @Operation(summary = "Cohort attendance summary (ADMIN or TRAINER)",
            description = "Attendance percentage, color, consecutive absences and active alerts for every enrolled student.")
    @PreAuthorize("hasAnyRole('ADMIN','TRAINER')")
    public ResponseEntity<CohortAttendanceSummaryResponse> getSummary(
            @PathVariable UUID programId,
            @PathVariable UUID cohortId) {

        return ResponseEntity.ok(attendanceService.getCohortSummary(programId, cohortId));
    }

    @GetMapping("/students/{studentId}")
    @Operation(summary = "A student's attendance history (ADMIN or TRAINER)",
            description = "Every recorded session for the student, newest first, with their current score.")
    @PreAuthorize("hasAnyRole('ADMIN','TRAINER')")
    public ResponseEntity<StudentAttendanceHistoryResponse> getStudentHistory(
            @PathVariable UUID programId,
            @PathVariable UUID cohortId,
            @PathVariable UUID studentId) {

        return ResponseEntity.ok(attendanceService.getStudentHistory(programId, cohortId, studentId));
    }
}
