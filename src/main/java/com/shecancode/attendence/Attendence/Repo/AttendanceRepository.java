package com.shecancode.attendence.Attendence.Repo;

import com.shecancode.attendence.Attendence.Model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {

    List<Attendance> findBySessionId(UUID sessionId);

    @Query("""
            SELECT a FROM Attendance a
            WHERE a.student.id = :studentId AND a.program.id = :programId
            ORDER BY a.attendanceRecordedDate DESC
            """)
    List<Attendance> findHistory(@Param("studentId") UUID studentId, @Param("programId") UUID programId);
}
