package com.shecancode.attendence.Attendence.Mapper;

import com.shecancode.attendence.Attendence.Model.Attendance;
import com.shecancode.attendence.Attendence.Model.AttendanceAlert;
import com.shecancode.attendence.Attendence.Model.AttendanceSession;
import com.shecancode.attendence.Attendence.dao.AttendanceAlertResponse;
import com.shecancode.attendence.Attendence.dao.AttendanceResponse;
import com.shecancode.attendence.Attendence.dao.StudentAttendanceRequestDto;
import com.shecancode.attendence.auth.model.AppUser;
import com.shecancode.attendence.registration.Model.Cohort;
import com.shecancode.attendence.registration.Model.Student;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class AttendanceMapper {

    public static Attendance toAttendance(StudentAttendanceRequestDto studentDto,
                                          Student student,
                                          AttendanceSession session,
                                          AppUser recorder) {
        Attendance attendance = Attendance.builder()
                .session(session)
                .student(student)
                .program(session.getProgram())
                .cohort(session.getCohort())
                .attendanceRecordedDate(session.getSessionDate())
                .build();
        applyEntry(attendance, studentDto, recorder);
        return attendance;
    }

    /** Copies the editable fields of an entry onto a (new or existing) attendance row. */
    public static void applyEntry(Attendance attendance, StudentAttendanceRequestDto dto, AppUser recorder) {
        attendance.setAttendanceStatus(dto.getAttendanceStatus());
        // Absent students have no check-in time.
        attendance.setCheckInTime(dto.getAttendanceStatus().requiresCheckInTime() ? dto.getCheckInTime() : null);
        attendance.setRemarks(dto.getRemarks());
        attendance.setRecordedById(recorder.getId());
        attendance.setRecordedByName(displayName(recorder));
    }

    public static AttendanceResponse toResponseDTO(Attendance attendance, LocalDate today) {
        Cohort cohort = attendance.getCohort();
        LocalDate graduationDate = cohort.getEndDate();

        return AttendanceResponse.builder()
                .attendanceId(attendance.getAttendanceId())
                .sessionId(attendance.getSession().getId())
                .studentId(attendance.getStudent().getId())
                .studentName(studentName(attendance.getStudent()))
                .cohortId(cohort.getId())
                .cohortNumber(cohort.getCohortNumber())
                .programId(attendance.getProgram().getId())
                .programName(attendance.getProgram().getProgramName())
                .attendanceStatus(attendance.getAttendanceStatus().name())
                .checkInTime(attendance.getCheckInTime())
                .remarks(attendance.getRemarks())
                .attendanceRecordedDate(attendance.getAttendanceRecordedDate())
                .graduationDate(graduationDate)
                .daysRemainingUntilGraduation(
                        (int) Math.max(0, ChronoUnit.DAYS.between(today, graduationDate)))
                .recordedById(attendance.getRecordedById())
                .recordedByName(attendance.getRecordedByName())
                .createdAt(attendance.getCreatedAt())
                .updatedAt(attendance.getUpdatedAt())
                .build();
    }

    public static AttendanceAlertResponse toAlertResponse(AttendanceAlert alert) {
        return AttendanceAlertResponse.builder()
                .alertId(alert.getId())
                .alertType(alert.getAlertType())
                .status(alert.getStatus())
                .absenceCount(alert.getAbsenceCount())
                .studentId(alert.getStudent().getId())
                .studentName(studentName(alert.getStudent()))
                .cohortId(alert.getCohort().getId())
                .cohortNumber(alert.getCohort().getCohortNumber())
                .programId(alert.getProgram().getId())
                .programName(alert.getProgram().getProgramName())
                .triggeredOnDate(alert.getTriggeredOnDate())
                .triggeredByName(alert.getTriggeredByName())
                .createdAt(alert.getCreatedAt())
                .resolvedAt(alert.getResolvedAt())
                .studentNotifiedAt(alert.getStudentNotifiedAt())
                .trainerNotifiedAt(alert.getTrainerNotifiedAt())
                .build();
    }

    /** Invited students have no name until they complete their profile; fall back to email. */
    public static String studentName(Student student) {
        String first = student.getStudentFirstName();
        String last = student.getStudentLastName();
        if (first == null && last == null) return student.getEmail();
        return ((first == null ? "" : first) + " " + (last == null ? "" : last)).trim();
    }

    public static String displayName(AppUser user) {
        return user.getFullName() != null && !user.getFullName().isBlank()
                ? user.getFullName()
                : user.getUsername();
    }
}
