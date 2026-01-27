package com.shecancode.attendence.AttendenceMgt.dao;

import com.shecancode.attendence.AttendenceMgt.Enum.Status;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Builder
@Data
@Setter
@Getter
public class StudentRequestDao {

    private String student_FirstName;

    private String student_Lastname;

    private int phoneNumber;

    private String email;

    private int home_address;

    private int cohort_Id;

    private Status status;

    private String current_occupation;

    private Date registration_period;
}
