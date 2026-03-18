package com.shecancode.attendence.registration.dao;

import com.shecancode.attendence.registration.Model.Cohort;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Builder
@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProgramRequestDao {

    private UUID programId;

    @NotBlank(message = "program name cannot be blank")
    private String programName;

    private Integer programDuration;

    private LocalDate programStartDate;

    private LocalDate programEndDate;

    private Cohort cohort;

}
