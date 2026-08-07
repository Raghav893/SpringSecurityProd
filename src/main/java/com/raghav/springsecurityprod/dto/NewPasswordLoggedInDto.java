package com.raghav.springsecurityprod.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NewPasswordLoggedInDto {
    @NotBlank
    @Size(min = 8)
    String CurrentPassword;

    @NotBlank
    @Size(min = 8)
    String NewPassword;

}
