package com.shecancode.attendence.Attendence.Repo;

import com.shecancode.attendence.Attendence.Enum.AttendanceStatus;
import com.shecancode.attendence.Attendence.Model.Attendance;
import com.shecancode.attendence.registration.Model.Program;
import com.shecancode.attendence.registration.Model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {
    // Standard naming: count + By + [Property] + And + [Property]
    long countByStudentAndProgramAndAttendanceStatus(Student student, Program program, AttendanceStatus status);

    @Query("SELECT COUNT(DISTINCT a.attendanceRecordedDate) FROM Attendance a WHERE a.program.id = :programId")
    long countDistinctDatesByProgramId(@Param("programId") UUID programId);

    // Your existing duplicate check method
    @Query("SELECT a.student.id FROM Attendance a WHERE a.attendanceRecordedDate = :date AND a.cohort.id = :cohortId")
    Set<UUID> findStudentIdsByDateAndCohort(@Param("date") LocalDate date, @Param("cohortId") UUID cohortId);

    // 1. For ParticipantService (Single Student - used for Health Sync)
    List<Attendance> findByStudentAndProgramOrderByAttendanceRecordedDateDesc(Student student, Program program);

    // 2. For AttendanceService (Multiple Students - used for Bulk Update)
    // We use "In" to handle the List of IDs
    List<Attendance> findByAttendanceRecordedDateAndCohortIdAndStudentIdIn(
            LocalDate date,
            UUID cohortId,
            Collection<UUID> studentIds
    );
}
