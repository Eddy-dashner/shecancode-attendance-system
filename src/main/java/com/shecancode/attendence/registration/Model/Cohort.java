package com.shecancode.attendence.registration.Model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;


@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cohort")
public class Cohort {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "cohort_id")
    private UUID id;

    @Column(name = "cohort_number", unique = true, nullable = false)
    private String cohortNumber;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    // A cohort belongs to exactly one program (Program 1 ── * Cohort).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private Program program;
}
