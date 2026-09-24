package com.shecancode.attendence.registration.Repository;

import com.shecancode.attendence.registration.Enum.Status;
import com.shecancode.attendence.registration.Model.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudentRepository extends JpaRepository<Student, UUID>, JpaSpecificationExecutor<Student> {
    boolean existsByEmail(String email);

    Optional<Student> findByEmail(String email);

    Student findStudentByStatus(Status status);

    Optional<Student> findByIdAndDeletedAtIsNull(UUID id);

    List<Student> findByCohort_IdAndDeletedAtIsNullOrderByStudentFirstNameAscStudentLastNameAsc(UUID cohortId);

    long countByCohort_IdAndDeletedAtIsNull(UUID cohortId);

    long countByProgram_IdAndStatusAndDeletedAtIsNull(UUID programId, Status status);

    @Override
    @EntityGraph(attributePaths = {"program", "cohort"})
    Page<Student> findAll(Specification<Student> spec, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"program", "cohort"})
    List<Student> findAll(Specification<Student> spec, Sort sort);
}
