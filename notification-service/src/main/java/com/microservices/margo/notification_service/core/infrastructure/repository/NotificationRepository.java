package com.microservices.margo.notification_service.core.infrastructure.repository;

import com.microservices.margo.notification_service.core.infrastructure.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@SuppressWarnings("NullableProblems")
@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID> {}

