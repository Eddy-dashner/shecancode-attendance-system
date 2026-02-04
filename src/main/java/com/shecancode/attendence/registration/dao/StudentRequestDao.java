package com.shecancode.attendence.registration.dao;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

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

    private String currentOccupation;

    private String programName;

    private String cohortNumber;
}
