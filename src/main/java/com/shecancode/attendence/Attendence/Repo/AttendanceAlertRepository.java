package com.shecancode.attendence.Attendence.Repo;

import com.shecancode.attendence.Attendence.Enum.AlertStatus;
import com.shecancode.attendence.Attendence.Enum.AlertType;
import com.shecancode.attendence.Attendence.Model.AttendanceAlert;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttendanceAlertRepository extends JpaRepository<AttendanceAlert, UUID> {

    Optional<AttendanceAlert> findByStudentIdAndProgramIdAndAlertTypeAndStatus(
            UUID studentId, UUID programId, AlertType alertType, AlertStatus status);

    @EntityGraph(attributePaths = {"student", "program", "cohort"})
    List<AttendanceAlert> findByCohortIdAndStatusOrderByCreatedAtDesc(UUID cohortId, AlertStatus status);

    @EntityGraph(attributePaths = {"student", "program", "cohort"})
    List<AttendanceAlert> findByStatusOrderByCreatedAtDesc(AlertStatus status);

    // Loaded with everything the alert emails need, so the Kafka consumer can
    // work outside a transaction.
    @EntityGraph(attributePaths = {"student", "program", "cohort"})
    List<AttendanceAlert> findByIdIn(Collection<UUID> ids);
}
