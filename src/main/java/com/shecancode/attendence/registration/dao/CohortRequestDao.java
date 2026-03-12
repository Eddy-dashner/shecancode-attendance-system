package com.shecancode.attendence.registration.dao;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;

@Builder
@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CohortRequestDao {
    @NotBlank(message = "Cohort number cannot be blank")
    private String cohortNumber;
    private LocalDate startDate;

    private LocalDate endDate;
}
