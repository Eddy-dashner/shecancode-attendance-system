package com.shecancode.attendence.registration.dao;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.shecancode.attendence.registration.Enum.Status;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Builder
@Data
public class StudentResponseDao {

    private UUID studentId;

    private String studentFirstName;

    private String studentLastName;

    private String phoneNumber;

    private String email;

    private String homeAddress;

    private String programName;

    private Status status;

    private String currentOccupation;

    private LocalDate programStartedDate;

    private LocalDate estimateGraduationDate;

    private int daysRemainingToGraduate;

    private String cohortNumber;

    private UUID cohortId;

    private UUID programId;

    // Only filled on single-student views (my profile, student detail); omitted in lists.
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private StudentProfileResponse profile;
}
