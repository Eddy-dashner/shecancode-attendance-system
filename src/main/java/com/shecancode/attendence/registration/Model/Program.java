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
@Table(name = "PROGRAM")

public class Program {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "program_id")
    private UUID id;

    @Column(name = "program_name", nullable = false)
    private String programName;

    @Column(name = "program_Duration", nullable = false)
    private Integer programDuration;

    @Column(name = "program_start_date", nullable = false)
    private LocalDate programStartDate;

    @Column(name = "program_end_date", nullable = false)
    private LocalDate programEndDate;


//    @Column(name = "days_to_graduate")
//    private Integer daysRemainingUntilGraduation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cohort_id", nullable = false)
    private Cohort cohort;

    private boolean isCalendarExpired() {
        return LocalDate.now().isAfter(this.programEndDate);

    }
}
