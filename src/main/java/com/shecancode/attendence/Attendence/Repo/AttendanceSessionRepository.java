package com.shecancode.attendence.Attendence.Repo;

import com.shecancode.attendence.Attendence.Model.AttendanceSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, UUID> {

    Optional<AttendanceSession> findByCohortIdAndSessionDate(UUID cohortId, LocalDate sessionDate);
}
