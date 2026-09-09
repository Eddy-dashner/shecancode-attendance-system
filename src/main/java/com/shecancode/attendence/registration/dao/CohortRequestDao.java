package com.shecancode.attendence.registration.dao;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Builder
@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Cohort creation payload")
public class CohortRequestDao {
    @NotBlank(message = "Cohort number cannot be blank")
    @Schema(example = "Cohort-11", description = "Unique cohort identifier")
    private String cohortNumber;

    @NotNull(message = "programId is required")
    @Schema(description = "ID of the existing program this cohort belongs to")
    private UUID programId;

    @Schema(example = "2026-03-01")
    private LocalDate startDate;

    @Schema(example = "2026-09-01")
    private LocalDate endDate;
}
