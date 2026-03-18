package com.shecancode.attendence.registration.dao;

import com.shecancode.attendence.registration.Model.Cohort;
import lombok.*;

import java.time.LocalDate;

@Builder
@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProgramResponseDao {
    private String programName;

    private Integer programDuration;

    private LocalDate programStartDate;

    private LocalDate programEndDate;

    private Cohort cohort;
}
