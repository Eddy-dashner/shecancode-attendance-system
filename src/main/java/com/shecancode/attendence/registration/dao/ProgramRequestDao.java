package com.shecancode.attendence.registration.dao;

import com.shecancode.attendence.registration.Model.Cohort;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Program creation payload. The cohort is taken from the path {cohortNumber}.")
public class ProgramRequestDao {

    @Schema(hidden = true)
    private UUID programId;

    @NotBlank(message = "program name cannot be blank")
    @Schema(example = "Frontend", description = "Unique program name")
    private String programName;

    @Schema(example = "6", description = "Duration in months")
    private Integer programDuration;

    @Schema(example = "2026-03-01")
    private LocalDate programStartDate;

    @Schema(example = "2026-09-01")
    private LocalDate programEndDate;

    @Schema(hidden = true)
    private Cohort cohort;

}
