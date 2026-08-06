package com.raghav.springsecurityprod.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NewPasswordDetailsDto {
    @NotBlank
    private String rawToken;
    @NotBlank
    private String password;
}
