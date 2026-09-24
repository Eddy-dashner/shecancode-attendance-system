package com.shecancode.attendence.registration.dao;

import com.shecancode.attendence.registration.Enum.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Change a student's status")
public class StudentStatusRequest {
    @NotNull(message = "status is required")
    @Schema(example = "INACTIVE", allowableValues = {"ACTIVE", "INACTIVE", "GRADUATED", "DROPPED_OUT"})
    private Status status;
}
