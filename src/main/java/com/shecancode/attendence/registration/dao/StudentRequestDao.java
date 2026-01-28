package com.shecancode.attendence.registration.dao;

import com.shecancode.attendence.registration.Enum.Status;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Builder
@Data
@Setter
@Getter
public class StudentRequestDao {

    private UUID studentId;

    private String student_FirstName;

    private String student_Lastname;

    private int phoneNumber;

    private String email;

    private int home_address;

    private int cohort_Id;

    private Status status;

    private Date startDate;

    private Date graduationDate;
}
