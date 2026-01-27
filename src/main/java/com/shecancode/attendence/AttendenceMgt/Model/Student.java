package com.shecancode.attendence.AttendenceMgt.Model;

import com.shecancode.attendence.AttendenceMgt.Enum.Status;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

@Getter
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID studentId;

    private String student_FirstName;

    private String student_Lastname;

    private int phoneNumber;

    private String email;

    private int home_address;

    private int cohort_Id;

    private Status status;

    private String current_occupation;

    private Date registration_period;

    @Override
    public String toString() {
        return "Student{" +
                "studentId=" + studentId +
                ", student_FirstName='" + student_FirstName + '\'' +
                ", student_Lastname='" + student_Lastname + '\'' +
                ", phoneNumber=" + phoneNumber +
                ", email='" + email + '\'' +
                ", home_address=" + home_address +
                ", cohort_Id=" + cohort_Id +
                ", status=" + status +
                ", current_occupation='" + current_occupation + '\'' +
                ", registration_period=" + registration_period +
                '}';
    }
}
