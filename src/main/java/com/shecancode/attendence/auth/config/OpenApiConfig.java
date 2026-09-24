package com.shecancode.attendence.auth.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "SheCanCODE Attendance & Student Management System",
        version = "2.0.0"
    ),
    security = @SecurityRequirement(name = "bearerAuth"),
    // Declares the display order and description of every Swagger section.
    tags = {
        @Tag(name = "Authentication", description = "Register (ADMIN), login, and student account activation"),
        @Tag(name = "Administration", description = "Admin setup for Cohorts and Programs"),
        @Tag(name = "Trainers", description = "Trainer invitation (ADMIN only)"),
        @Tag(name = "Students", description = "Student enrolment, activation profile and lookup"),
        @Tag(name = "Attendance", description = "Cohort registers, summaries, student history and absence alerts")
    }
)
@SecurityScheme(
    name = "bearerAuth",
    description = "JWT Bearer token. Obtain one from POST /api/v1/auth/login",
    scheme = "bearer",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}
