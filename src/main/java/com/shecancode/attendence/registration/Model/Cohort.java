package com.shecancode.attendence.registration.Model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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

//    @Column(name = "graduation_date", nullable = false)
//    private LocalDate graduationDate;

//    @OneToMany(mappedBy = "cohort", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Program> programs = new ArrayList<>();
}
