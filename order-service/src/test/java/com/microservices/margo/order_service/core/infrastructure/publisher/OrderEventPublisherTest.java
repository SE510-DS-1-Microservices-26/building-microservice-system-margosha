package com.microservices.margo.order_service.core.infrastructure.publisher;

import com.microservices.margo.order_service.core.application.event.OrderCreatedEvent;
import com.microservices.margo.order_service.core.domain.Order;
import com.microservices.margo.order_service.core.infrastructure.config.CorrelationProperties;
import com.microservices.margo.order_service.core.infrastructure.config.RabbitMQProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.MDC;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.UUID;

import static com.microservices.margo.order_service.TestData.CORRELATION_ID_HEADER;
import static com.microservices.margo.order_service.TestData.MDC_KEY;
import static com.microservices.margo.order_service.TestData.pendingOrder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("OrderEventPublisher tests")
@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class OrderEventPublisherTest {

    private static final String EXCHANGE = "core";
    private static final String ROUTING_KEY = "core-item.created";

    @Mock
    private CorrelationProperties correlationProperties;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private RabbitMQProperties rabbitMQProperties;

    @InjectMocks
    private OrderEventPublisher orderEventPublisher;

    @BeforeEach
    void setUp(){
        when(correlationProperties.header()).thenReturn(CORRELATION_ID_HEADER);
        when(correlationProperties.key()).thenReturn(MDC_KEY);
        when(rabbitMQProperties.exchange()).thenReturn(EXCHANGE);
        when(rabbitMQProperties.routingKey()).thenReturn(ROUTING_KEY);
    }

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    @DisplayName("publishes OrderCreatedEvent with correct fields")
    void publishOrderCreated_publishesEventWithCorrectFields() {
        // Arrange
        Order order = pendingOrder();

        // Act
        orderEventPublisher.publishOrderCreated(order);

        // Assert
        ArgumentCaptor<OrderCreatedEvent> captor = ArgumentCaptor.forClass(OrderCreatedEvent.class);
        verify(rabbitTemplate).convertAndSend(
                eq("core"),
                eq("core-item.created"),
                captor.capture(),
                any(MessagePostProcessor.class)
        );
        OrderCreatedEvent event = captor.getValue();
        assertThat(event.eventId()).isNotNull();
        assertThat(event.occurredAt()).isNotNull();
        assertThat(event.orderId()).isEqualTo(order.id());
        assertThat(event.ownerUserId()).isEqualTo(order.ownerUserId());
        assertThat(event.payload()).contains(order.itemName());
    }

    @Test
    @DisplayName("uses correlationId from MDC when present")
    void publishOrderCreated_usesCorrelationIdFromMdc() {
        // Arrange
        Order order = pendingOrder();
        String expectedCorrelationId = UUID.randomUUID().toString();
        MDC.put(correlationProperties.key(), expectedCorrelationId);

        // Act
        orderEventPublisher.publishOrderCreated(order);

        // Assert
        ArgumentCaptor<OrderCreatedEvent> captor = ArgumentCaptor.forClass(OrderCreatedEvent.class);
        verify(rabbitTemplate).convertAndSend(eq(EXCHANGE), eq(ROUTING_KEY),
                captor.capture(), any(MessagePostProcessor.class));
        assertThat(captor.getValue().correlationId().toString()).isEqualTo(expectedCorrelationId);
    }
}