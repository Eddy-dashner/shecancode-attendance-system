package com.shecancode.attendence.registration.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.shecancode.attendence.registration.deserializer.ProgramsDeserializer;
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
    
    @JsonProperty("cohort_number")
    @Column(name = "cohort_number", unique = true, nullable = false)
    private String cohortNumber;

    @JsonProperty("cohort_starting_date")
    @Column(name = "cohorts_starting_date", unique = true, nullable = false)
    private LocalDate startDate;

    @JsonProperty("cohort_endDate")
    @Column(name = "cohorts_endDate", unique = true, nullable = false)
    private LocalDate endDate;

    @JsonProperty("graduation_Date")
    @Column(name = "graduation_Date", unique = true, nullable = false)
    private LocalDate graduationDate;

    @JsonProperty("program_Is")
    @JsonDeserialize(using = ProgramsDeserializer.class)
    @ManyToOne
    @JoinColumn(name = "program_id",nullable = false)
    private Programs program;
}
