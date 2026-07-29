package com.raghav.springsecurityprod.repo;

import com.raghav.springsecurityprod.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {
}
