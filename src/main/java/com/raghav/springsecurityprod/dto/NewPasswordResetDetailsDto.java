package com.raghav.springsecurityprod.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NewPasswordResetDetailsDto {
    @NotBlank
    private String rawToken;
    @NotBlank
    @Size(min = 8)
    private String password;
}
