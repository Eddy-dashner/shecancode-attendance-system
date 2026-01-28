package com.shecancode.attendence.registration.dao;

import com.shecancode.attendence.registration.Enum.Status;
import lombok.Builder;

import java.util.Date;
import java.util.UUID;

@Builder
public class StudentResponseDao {

    private UUID studentId;

    private String student_FirstName;

    private String student_Lastname;

    private int phoneNumber;

    private String email;

    private int home_address;

    private int cohort_Id;

    private Status status;

    private String current_occupation;

    private Date startDate;

    private Date graduationDate;
}
