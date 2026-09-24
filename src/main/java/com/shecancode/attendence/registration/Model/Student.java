package com.shecancode.attendence.registration.Model;

import com.shecancode.attendence.auth.model.AppUser;
import com.shecancode.attendence.registration.Enum.Status;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
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
    @Column(name = "student_id")
    private UUID id;

    @Column(name = "student_first_name")
    private String studentFirstName;

    @Column(name = "student_last_name")
    private String studentLastName;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "home_address")
    private String homeAddress;

    @Column(name = "current_occupation")
    private String currentOccupation;

    @Enumerated(EnumType.STRING)
    @Column(name = "student_status")
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cohort_id")
    private Cohort cohort;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id")
    private Program program;

    // The login account backing this student. Created (disabled) when the admin
    // adds the student and activated by the student via an email token.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUser user;

    // Soft delete: set when an admin deletes the student; history is kept.
    @Column(name = "deleted_at")
    private Instant deletedAt;

    public String getFullName() {
        return studentFirstName + " " + studentLastName;
    }
}
