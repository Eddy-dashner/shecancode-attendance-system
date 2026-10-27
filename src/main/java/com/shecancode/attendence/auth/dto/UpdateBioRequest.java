package com.shecancode.attendence.auth.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBioRequest {
    // Null or blank clears the bio.
    @Size(max = 500, message = "Bio must be at most 500 characters")
    private String bio;
}
