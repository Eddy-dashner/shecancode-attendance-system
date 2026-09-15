package com.shecancode.attendence.registration.dao;

import lombok.*;

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
}
