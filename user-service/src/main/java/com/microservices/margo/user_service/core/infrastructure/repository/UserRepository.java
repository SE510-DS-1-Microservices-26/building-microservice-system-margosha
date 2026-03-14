package com.microservices.margo.user_service.core.infrastructure.repository;

import com.microservices.margo.user_service.core.infrastructure.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@SuppressWarnings("NullableProblems")
@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    boolean existsByEmail(String email);
}

