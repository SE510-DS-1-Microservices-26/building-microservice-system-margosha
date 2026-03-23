package com.microservices.margo.notification_service.core.infrastructure.listener;

import com.microservices.margo.notification_service.core.application.event.OrderCreatedEvent;
import com.microservices.margo.notification_service.core.application.usecase.StoreNotificationUseCase;
import com.microservices.margo.notification_service.core.infrastructure.config.CorrelationProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import java.time.Instant;
import java.util.UUID;

 import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("OrderCreatedListener tests")
@ExtendWith(MockitoExtension.class)
class OrderCreatedListenerTest {
    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String MDC_KEY = "correlationId";

    @Mock
    private StoreNotificationUseCase storeNotification;

    @Mock
    private CorrelationProperties correlationProperties;

    @Mock
    private Jackson2JsonMessageConverter converter;

    @InjectMocks
    private OrderCreatedListener orderCreatedListener;

    private Message buildMessage(String correlationId) {
        MessageProperties props = new MessageProperties();
        if (correlationId != null) {
            props.setHeader("X-Correlation-Id", correlationId);
        }
        return new Message(new byte[0], props);
    }

    private OrderCreatedEvent anyEvent() {
        return new OrderCreatedEvent(
                UUID.randomUUID(), Instant.now(), UUID.randomUUID().toString(),
                UUID.randomUUID(), UUID.randomUUID(), "Order created: Latte x2 @ 5.99"
        );
    }

    @BeforeEach
    void setUp(){
        when(correlationProperties.header()).thenReturn(CORRELATION_ID_HEADER);
        when(correlationProperties.key()).thenReturn(MDC_KEY);
    }

    @Test
    @DisplayName("delegates to StoreNotificationUseCase on event received")
    void onOrderCreated_delegatesToUseCase() {
        // Arrange
        OrderCreatedEvent event = anyEvent();
        Message message = buildMessage("some-correlation-id");
        when(converter.fromMessage(message)).thenReturn(event);

        // Act
        orderCreatedListener.onOrderCreated(message);

        // Assert
        verify(storeNotification).execute(event);
    }

    @Test
    @DisplayName("sets correlationId in MDC from message header")
    void onOrderCreated_setsMdcCorrelationId() {
        // Arrange
        String correlationId = "test-id-123";
        Message message = buildMessage(correlationId);
        when(converter.fromMessage(message)).thenReturn(anyEvent());

        // Act
        orderCreatedListener.onOrderCreated(message);

        // Assert
        verify(storeNotification).execute(any());
    }

    @Test
    @DisplayName("uses 'unknown' as correlationId when header is absent")
    void onOrderCreated_usesUnknown_whenHeaderAbsent() {
        // Arrange
        Message message = buildMessage(null);
        OrderCreatedEvent event = anyEvent();
        when(converter.fromMessage(message)).thenReturn(event);

        // Act
        orderCreatedListener.onOrderCreated(message);

        // Assert
        verify(storeNotification).execute(event);
    }
}