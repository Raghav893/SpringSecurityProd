package com.raghav.springsecurityprod.repo;

import com.raghav.springsecurityprod.entity.ForgotPasswordToken;
import com.raghav.springsecurityprod.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ForgotPasswordTokenRepository extends JpaRepository<ForgotPasswordToken, UUID> {


    Optional<ForgotPasswordToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("update ForgotPasswordToken t set t.used = true where t.user=:user AND  t.used=false")
    void  invalidateAllUnusedForUser(@Param("user") User user);
}
