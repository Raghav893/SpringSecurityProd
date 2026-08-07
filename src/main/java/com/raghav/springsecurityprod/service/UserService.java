package com.raghav.springsecurityprod.service;

import com.raghav.springsecurityprod.dto.NewPasswordLoggedInDto;
import com.raghav.springsecurityprod.dto.RegisterRequest;
import com.raghav.springsecurityprod.entity.Role;
import com.raghav.springsecurityprod.entity.User;
import com.raghav.springsecurityprod.exceptions.EmailAlreadyExistsException;
import com.raghav.springsecurityprod.exceptions.EmailNotFoundException;
import com.raghav.springsecurityprod.exceptions.SamePasswordException;
import com.raghav.springsecurityprod.repo.RefreshTokenRepository;
import com.raghav.springsecurityprod.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("An account with this email already exists");
        }
        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .roles(Set.of(Role.ROLE_USER))
                .enabled(true)
                .emailVerified(false)
                .build();
        userRepository.save(user);
        return user;
    }
    @Transactional
    public void changePassword(NewPasswordLoggedInDto dto) throws InvalidCurrentPasswordException {
            String username = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
            User user = userRepository.findByEmail(username)
                    .orElseThrow(()-> new EmailNotFoundException("Email not found"));
        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new InvalidCurrentPasswordException("Current password is incorrect");
        }
        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw new SamePasswordException("New password cannot be same as old one");
        }
            user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
            refreshTokenRepository.revokeAllByUser(user);
            userRepository.save(user);
    }
}
