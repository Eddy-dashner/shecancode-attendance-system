package com.shecancode.attendence.registration.dao;

import com.shecancode.attendence.registration.Enum.Status;
import com.shecancode.attendence.registration.Model.Programs;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Builder
@Data
@Setter
@Getter
public class StudentRequestDao {

    private String studentFirstName;

    private String studentLastName;

    private String phoneNumber;

    private String email;

    private String homeAddress;

    private Status status;

    private String currentOccupation;

    private String programName;

    private UUID cohortId;
}
