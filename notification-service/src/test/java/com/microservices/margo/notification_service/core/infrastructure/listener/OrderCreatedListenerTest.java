package com.microservices.margo.notification_service.core.infrastructure.listener;

import com.microservices.margo.notification_service.core.application.event.OrderCreatedEvent;
import com.microservices.margo.notification_service.core.application.usecase.StoreNotificationUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@DisplayName("OrderCreatedListener tests")
@ExtendWith(MockitoExtension.class)
class OrderCreatedListenerTest {

    @Mock
    private StoreNotificationUseCase storeNotification;

    @InjectMocks
    private OrderCreatedListener orderCreatedListener;

    @Test
    @DisplayName("delegates to StoreNotificationUseCase on event received")
    void onOrderCreated_delegatesToUseCase() {
        // Arrange
        OrderCreatedEvent event = new OrderCreatedEvent(
                UUID.randomUUID(), Instant.now(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), "Order created: Latte x2 @ 5.99"
        );

        // Act
        orderCreatedListener.onOrderCreated(event);

        // Assert
        verify(storeNotification).execute(event);
    }
}