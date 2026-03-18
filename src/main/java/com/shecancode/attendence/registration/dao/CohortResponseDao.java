package com.shecancode.attendence.registration.dao;

import lombok.*;

import java.time.LocalDate;

@Builder
@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CohortResponseDao {
    private String cohortNumber;
    private LocalDate startDate;
    private LocalDate endDate;
}
