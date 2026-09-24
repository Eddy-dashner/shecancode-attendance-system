package com.shecancode.attendence.registration.Repository;

import com.shecancode.attendence.registration.Model.Cohort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CohortRepository extends JpaRepository<Cohort, UUID>, JpaSpecificationExecutor<Cohort> {

    // Deleted cohorts still reserve their number within the program.
    boolean existsByProgram_IdAndCohortNumber(UUID programId, String cohortNumber);

    boolean existsByProgram_IdAndCohortNumberAndIdNot(UUID programId, String cohortNumber, UUID id);

    @EntityGraph(attributePaths = "program")
    Optional<Cohort> findByIdAndDeletedAtIsNull(UUID id);

    long countByProgram_IdAndDeletedAtIsNull(UUID programId);

    @Override
    @EntityGraph(attributePaths = "program")
    List<Cohort> findAll(Specification<Cohort> spec, Sort sort);
}
