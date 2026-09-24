package com.shecancode.attendence.registration.dao;

import lombok.*;

import com.shecancode.attendence.registration.Enum.LifecycleStatus;

import java.time.LocalDate;
import java.util.UUID;

@Builder
@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProgramResponseDao {
    private UUID programId;

    private String programName;

    private Integer programDuration;

    private LocalDate programStartDate;

    private LocalDate programEndDate;

    private LifecycleStatus status;

    // Only filled when fetching a single program.
    private Long cohortCount;

    // ACTIVE students; only filled when fetching a single program.
    private Long participantCount;
}
