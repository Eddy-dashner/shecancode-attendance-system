package com.shecancode.attendence.registration.Model;

import com.shecancode.attendence.registration.Enum.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "student")
@Builder
@Getter
@Setter

@NoArgsConstructor
@AllArgsConstructor
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "student_id", updatable = false, nullable = false)
    private UUID studentId;

    @NotBlank(message = "First name is required")
    @Size(max = 100)
    @Column(name = "student_first_name", nullable = false)
    private String studentFirstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100)
    @Column(name = "student_last_name", nullable = false)
    private String studentLastName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Email(message = "Email should be valid")
    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "home_address")
    private String homeAddress;

    @ManyToOne
    @JoinColumn(name = "cohort_id")
    private Cohort cohort;

    @Enumerated(EnumType.STRING)
    @Column(name = "student_status", nullable = false)
    private Status status = Status.ACTIVE;

    @Column(name = "current_occupation")
    private String currentOccupation;

    @Column(name = "days_to_graduation")
    private Integer daysRemaining;

    @Column(name = "total_graduation_days")
    private Integer totalProgramDays;


}
