package com.raghav.springsecurityprod.controller;

import com.raghav.springsecurityprod.dto.NewPasswordDetailsDto;
import com.raghav.springsecurityprod.service.ForgotPasswordService;
import com.raghav.springsecurityprod.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class ForgotPasswordController {

    private final ForgotPasswordService forgotPasswordService;
    private final UserService userService;


    @PostMapping("/forgot-password")
    public ResponseEntity<?> reset(@RequestBody ForgotPasswordRequest request){
        String email = request.email();
        forgotPasswordService.issueVerificationToken(email);
        return ResponseEntity.ok(Map.of("message", "Password Reset Link set to registered email"));

    }
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody NewPasswordDetailsDto dto) {
        forgotPasswordService.verifyAndUpdatePassword(dto);
        return ResponseEntity.ok().build();
    }}
