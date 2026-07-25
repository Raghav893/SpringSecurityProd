package com.raghav.springsecurityprod.repo;

import com.raghav.springsecurityprod.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
}
