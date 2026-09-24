package com.shecancode.attendence.registration.Repository;

import com.shecancode.attendence.registration.Enum.Status;
import com.shecancode.attendence.registration.Model.Student;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

/** Optional student filters; a null argument means "don't filter on this". */
public final class StudentSpecs {

    private StudentSpecs() {}

    public static Specification<Student> matching(UUID programId, UUID cohortId, Status status, String search) {
        Specification<Student> spec = (root, query, cb) -> cb.isNull(root.get("deletedAt"));
        if (programId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("program").get("id"), programId));
        }
        if (cohortId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("cohort").get("id"), cohortId));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (search != null && !search.isBlank()) {
            String like = "%" + search.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("email")), like),
                    cb.like(cb.lower(root.get("studentFirstName")), like),
                    cb.like(cb.lower(root.get("studentLastName")), like)));
        }
        return spec;
    }
}
