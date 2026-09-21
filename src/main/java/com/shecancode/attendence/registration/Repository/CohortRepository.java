package com.shecancode.attendence.registration.Repository;

import com.shecancode.attendence.registration.Model.Cohort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CohortRepository extends JpaRepository<Cohort, UUID> {
    boolean existsByProgram_IdAndCohortNumber(UUID programId, String cohortNumber);
}
