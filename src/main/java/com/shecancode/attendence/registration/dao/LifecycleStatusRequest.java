package com.shecancode.attendence.registration.dao;

import com.shecancode.attendence.registration.Enum.LifecycleStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Open or close a program or cohort. CLOSED is read-only until reopened.")
public class LifecycleStatusRequest {
    @NotNull(message = "status is required")
    @Schema(example = "CLOSED")
    private LifecycleStatus status;
}
