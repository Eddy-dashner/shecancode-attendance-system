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
        title = "SheCanCODE Attendance System API",
        version = "1.0",
        description = "Attendance and stipend tracking system. Use the Authorize button to supply your JWT token."
    ),
    security = @SecurityRequirement(name = "bearerAuth"),
    // Declares the display order and description of every Swagger section.
    // Operations are grouped by who performs them, then by domain.
    tags = {
        @Tag(name = "Admin", description = "Actions performed by an ADMIN: user registration and student administration (invite, bulk-invite, list, drop-out)"),
        @Tag(name = "Student", description = "Actions a STUDENT performs on their own account: view and complete their profile"),
        @Tag(name = "Programs", description = "Program management (ADMIN only)"),
        @Tag(name = "Cohorts", description = "Cohort management within a program (ADMIN only)"),
        @Tag(name = "Trainers", description = "Trainer invitation (ADMIN only)"),
        @Tag(name = "Attendance", description = "Bulk attendance recording and updates (ADMIN or TRAINER)"),
        @Tag(name = "Authentication", description = "Public authentication flows: login, account activation and resend")
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
    // Configuration is fully declarative via annotations
}
