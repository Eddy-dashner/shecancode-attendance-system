package com.shecancode.attendence.registration.Model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.web.service.annotation.GetExchange;

import java.util.UUID;

@Builder
@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "PROGRAM")

public class Programs {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "program_id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "program_name", unique = true, nullable = false)
    private String programName;

    @Column(name = "program_running_period", nullable = false)
    private Integer requiredProgramDuration;

}
