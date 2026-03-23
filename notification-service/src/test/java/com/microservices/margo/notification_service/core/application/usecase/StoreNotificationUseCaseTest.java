package com.microservices.margo.notification_service.core.application.usecase;

import com.microservices.margo.notification_service.core.application.event.OrderCreatedEvent;
import com.microservices.margo.notification_service.core.application.mapper.NotificationMapper;
import com.microservices.margo.notification_service.core.infrastructure.entity.NotificationEntity;
import com.microservices.margo.notification_service.core.infrastructure.repository.NotificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("StoreNotificationUseCase tests")
@ExtendWith(MockitoExtension.class)
class StoreNotificationUseCaseTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private StoreNotificationUseCase storeNotificationUseCase;

    private OrderCreatedEvent buildEvent() {
        return new OrderCreatedEvent(
                UUID.randomUUID(), Instant.now(), UUID.randomUUID().toString(),
                UUID.randomUUID(), UUID.randomUUID(), "Order created: Latte x2 @ 5.99"
        );
    }

    @Test
    @DisplayName("saves notification when event is new")
    void execute_savesNotification_whenEventIsNew() {
        // Arrange
        OrderCreatedEvent event = buildEvent();
        NotificationEntity mappedEntity = NotificationEntity.builder()
                .eventId(event.eventId())
                .correlationId(event.correlationId())
                .coreItemId(event.orderId())
                .ownerUserId(event.ownerUserId())
                .payload(event.payload())
                .createdAt(Instant.now())
                .build();
        when(notificationMapper.toEntity(event)).thenReturn(mappedEntity);

        // Act
        storeNotificationUseCase.execute(event);

        // Assert
        ArgumentCaptor<NotificationEntity> captor = ArgumentCaptor.forClass(NotificationEntity.class);
        verify(notificationRepository).save(captor.capture());
        NotificationEntity saved = captor.getValue();
        assertThat(saved.getEventId()).isEqualTo(event.eventId());
        assertThat(saved.getCoreItemId()).isEqualTo(event.orderId());
        assertThat(saved.getOwnerUserId()).isEqualTo(event.ownerUserId());
        assertThat(saved.getPayload()).isEqualTo(event.payload());
    }

    @Test
    @DisplayName("ignores duplicate event without throwing")
    void execute_ignoresDuplicate_whenEventIdAlreadyExists() {
        // Arrange
        OrderCreatedEvent event = buildEvent();
        doThrow(DataIntegrityViolationException.class).when(notificationRepository).save(any());

        // Act & Assert
        storeNotificationUseCase.execute(event);
        verify(notificationRepository).save(any());
    }
}