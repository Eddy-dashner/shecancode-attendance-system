package com.shecancode.attendence.Attendence.Repo;

import com.shecancode.attendence.Attendence.Model.Attendance;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AttendanceRepository extends JpaRepository<Attendance, UUID>, JpaSpecificationExecutor<Attendance> {

    List<Attendance> findBySessionId(UUID sessionId);

    @Query("""
            SELECT a FROM Attendance a
            WHERE a.student.id = :studentId AND a.program.id = :programId
            ORDER BY a.attendanceRecordedDate DESC
            """)
    List<Attendance> findHistory(@Param("studentId") UUID studentId, @Param("programId") UUID programId);

    /** (studentId, status) pairs for a cohort, newest session first; enough to score everyone. */
    @Query("""
            SELECT a.student.id, a.attendanceStatus FROM Attendance a
            WHERE a.cohort.id = :cohortId
            ORDER BY a.attendanceRecordedDate DESC
            """)
    List<Object[]> findStatusesByCohort(@Param("cohortId") UUID cohortId);

    @Override
    @EntityGraph(attributePaths = {"student", "cohort", "program", "session"})
    List<Attendance> findAll(Specification<Attendance> spec, Sort sort);
}
