package com.shecancode.attendence.Attendence.Mapper;

import com.shecancode.attendence.Attendence.Model.Attendance;
import com.shecancode.attendence.Attendence.Enum.AttendanceStatus;
import com.shecancode.attendence.Attendence.dto.StudentAttendanceRequestDto;
import com.shecancode.attendence.Attendence.dto.AttendanceResponse;
import com.shecancode.attendence.registration.Model.Cohort;
import com.shecancode.attendence.registration.Model.Program;
import com.shecancode.attendence.registration.Model.Student;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class AttendanceMapper {
    // Map from single student attendance DTO to Attendance entity
    public static Attendance toAttendance(StudentAttendanceRequestDto studentDto,
                                          Student student,
                                          Program program,
                                          Cohort cohort,
                                          UUID recordedById,
                                          String recordedByName,
                                          LocalDate attendanceDate) {

        return Attendance.builder()
                // ID is omitted; Hibernate generates it automatically
                .student(student)
                .program(program)
                .cohort(cohort)
                // Since DTO and Entity both use the Enum, no need for valueOf()
                .attendanceStatus(studentDto.getAttendanceStatus())
                .attendanceRecordedDate(attendanceDate)
                .checkInTime(studentDto.getCheckInTime())
                .remarks(studentDto.getRemarks())
                .recordedById(recordedById)
                .recordedByName(recordedByName)
                .build();
    }

    // Map single Attendance entity to AttendanceResponse DTO
    public static AttendanceResponse toResponseDTO(Attendance attendance, Integer daysRemaining) {
        if (attendance == null) return null;

        return AttendanceResponse.builder()
                .attendanceId(attendance.getAttendanceId())
                .studentId(attendance.getStudent().getId())
                // Using a helper for name concatenation to keep it clean
                .studentName(formatFullName(attendance.getStudent()))
                .cohortId(attendance.getCohort() != null ? attendance.getCohort().getId() : null)
                .cohortNumber(attendance.getCohort() != null ? attendance.getCohort().getCohortNumber() : "N/A")
                .programId(attendance.getProgram().getId())
                .programName(attendance.getProgram().getProgramName())
                .attendanceStatus(attendance.getAttendanceStatus().name())
                .checkInTime(attendance.getCheckInTime())
                .remarks(attendance.getRemarks())
                .daysRemainingUntilGraduation(daysRemaining)
                .attendanceRecordedDate(attendance.getAttendanceRecordedDate())
                .recordedById(attendance.getRecordedById())
                .recordedByName(attendance.getRecordedByName())
                .createdAt(attendance.getCreatedAt())
                .updatedAt(attendance.getUpdatedAt())
                .build();
    }

    public static List<AttendanceResponse> toResponseDTOList(List<Attendance> attendances, Integer daysRemaining) {
        if (attendances == null) return Collections.emptyList();

        return attendances.stream()
                .map(attendance -> toResponseDTO(attendance, daysRemaining))
                .toList(); // Java 16+ syntax
    }

    private static String formatFullName(Student student) {
        return String.format("%s %s", student.getStudentFirstName(), student.getStudentLastName()).trim();
    }
}

