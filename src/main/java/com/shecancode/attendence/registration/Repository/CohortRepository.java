package com.shecancode.attendence.registration.Repository;

import com.shecancode.attendence.registration.Model.Cohort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CohortRepository extends JpaRepository<Cohort, UUID> {
    Optional<Cohort> findByCohortNumber(String cohortNumber);
}
