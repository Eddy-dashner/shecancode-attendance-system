package com.shecancode.attendence.Attendence.Controller;

import com.shecancode.attendence.Attendence.Enum.AttendanceStatus;
import com.shecancode.attendence.Attendence.Service.AttendanceService;
import com.shecancode.attendence.Attendence.dao.AttendanceReportResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/programs/{programId}/attendance")
@Tag(name = "Attendance")
public class ProgramAttendanceController {
    private final AttendanceService attendanceService;

    public ProgramAttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping
    @Operation(summary = "Program attendance for a date range (ADMIN or TRAINER)",
            description = "All cohorts of the program, newest first, with totals. from/to default to the last 30 " +
                    "days ending today; a range can cover at most 366 days. Optionally filter by cohortId and status.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Report returned"),
            @ApiResponse(responseCode = "400", description = "from after to, range too long, or cohort not in program", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is a STUDENT", content = @Content),
            @ApiResponse(responseCode = "404", description = "Program or cohort not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ADMIN','TRAINER')")
    public ResponseEntity<AttendanceReportResponse> getProgramAttendance(
            @PathVariable UUID programId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) UUID cohortId,
            @RequestParam(required = false) AttendanceStatus status) {

        return ResponseEntity.ok(attendanceService.getProgramAttendance(programId, from, to, cohortId, status));
    }
}
