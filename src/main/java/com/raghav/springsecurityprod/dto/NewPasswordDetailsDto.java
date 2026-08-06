package com.raghav.springsecurityprod.dto;

import lombok.Data;

@Data
public class NewPasswordDetailsDto {
    private String rawToken;
    private String password;
}
