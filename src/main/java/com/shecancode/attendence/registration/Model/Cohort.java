package com.shecancode.attendence.registration.Model;

import jakarta.persistence.*;
import lombok.*;

import com.shecancode.attendence.registration.Enum.LifecycleStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;


@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cohort", uniqueConstraints = @UniqueConstraint(
        name = "uk_cohort_program_number", columnNames = {"program_id", "cohort_number"}))
public class Cohort {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "cohort_id")
    private UUID id;

    // Unique per program, not globally: "Cohort 1" may exist in every program.
    @Column(name = "cohort_number", nullable = false)
    private String cohortNumber;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    // A cohort belongs to exactly one program (Program 1 ── * Cohort).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private Program program;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private LifecycleStatus status = LifecycleStatus.OPEN;

    // Soft delete: set when an admin deletes the cohort; null while it exists.
    @Column(name = "deleted_at")
    private Instant deletedAt;

    /** Read-only when the cohort or its program is closed. */
    public boolean isReadOnly() {
        return status == LifecycleStatus.CLOSED || (program != null && program.isClosed());
    }
}
