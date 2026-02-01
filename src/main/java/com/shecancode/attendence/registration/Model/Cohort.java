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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cohort_id", unique = true, nullable = false)
    private UUID id;
    @Column(name = "cohort_number", unique = true, nullable = false)
    private String cohortNumber;

    @Column(name = "cohorts_starting_date", unique = true, nullable = false)
    private LocalDate startDate;

    @Column(name = "cohorts_endDate", unique = true, nullable = false)
    private LocalDate endDate;

    @Column(name = "graduation_Date", unique = true, nullable = false)
    private LocalDate graduationDate;

    @ManyToOne
    @JoinColumn(name = "program_id",nullable = false)
    private Programs program;
}
