//package com.shecancode.attendence.Attendence.Model;
//
//import com.shecancode.attendence.Attendence.Enum.ProgressColor;
//import com.shecancode.attendence.registration.Model.Program;
//import com.shecancode.attendence.registration.Model.Student;
//import jakarta.persistence.*;
//import lombok.*;
//
//
//import java.time.LocalDate;
//import java.util.UUID;
//
//@Entity
//@Builder
//@AllArgsConstructor
//@NoArgsConstructor
//@Getter
//@Setter
//public class ParticipantProgress {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.UUID)
//    private UUID id;
//
//    @ManyToOne
//    private Student student;
//
//    @ManyToOne
//    private Program program;
//
//    private int attendancePoints;
//
//    private double attendancePercentage;
//
//    @Enumerated(EnumType.STRING)
//    private ProgressColor color; // GREEN, YELLOW, RED
//
//    private int consecutiveAbsences;
//
//    private LocalDate lastUpdated;
//
//}
