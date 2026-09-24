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
public class CohortResponseDao {
    private UUID cohortId;
    private String cohortNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private UUID programId;
    private String programName;
    private LifecycleStatus status;
    // True when the cohort or its program is closed.
    private boolean readOnly;
}
