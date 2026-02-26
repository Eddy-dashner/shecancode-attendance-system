package com.shecancode.attendence.Attendence.Repo;

import com.shecancode.attendence.Attendence.Enum.AttendanceStatus;
import com.shecancode.attendence.Attendence.Model.Attendance;
import com.shecancode.attendence.registration.Model.Program;
import com.shecancode.attendence.registration.Model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {
    List<Attendance> findByStudentAndProgramOrderByAttendanceRecordedDateDesc(Student student, Program program);

//    int countByStudentAndProgramAndAttendanceStatus(Student student, Program program, AttendanceStatus attendanceStatus);
//
//    boolean existsByStudentAndAttendanceRecordedDate(Student student, LocalDate attendanceRecordedDate);

    @Query("SELECT a.student.id FROM Attendance a " +
            "WHERE a.attendanceRecordedDate = :attendanceDate " +
            "AND a.cohort.id = :cohortId")
    Set<UUID> findStudentIdsByDateAndCohort(
            @Param("attendanceDate") LocalDate attendanceDate,
            @Param("cohortId") UUID cohortId
    );
    @Query("SELECT a FROM Attendance a WHERE a.attendanceRecordedDate = :date " +
            "AND a.cohort.id = :cohortId " +
            "AND a.student.id IN :studentIds")
    List<Attendance> findByAttendanceRecordedDateAndCohortIdAndStudentIdIn(
            @Param("date") LocalDate date,
            @Param("cohortId") UUID cohortId,
            @Param("studentIds") List<UUID> studentIds
    );

}
