package com.raghav.springsecurityprod.service;

import com.raghav.springsecurityprod.dto.RegisterRequest;
import com.raghav.springsecurityprod.entity.Role;
import com.raghav.springsecurityprod.entity.User;
import com.raghav.springsecurityprod.exceptions.EmailAlreadyExistsException;
import com.raghav.springsecurityprod.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
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
}
