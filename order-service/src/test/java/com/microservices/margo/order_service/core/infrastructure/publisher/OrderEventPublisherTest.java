package com.microservices.margo.order_service.core.infrastructure.publisher;

import com.microservices.margo.order_service.core.application.event.OrderCreatedEvent;
import com.microservices.margo.order_service.core.domain.Order;
import com.microservices.margo.order_service.core.infrastructure.config.RabbitMQProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static com.microservices.margo.order_service.TestData.pendingOrder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("OrderEventPublisher tests")
@ExtendWith(MockitoExtension.class)
class OrderEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private RabbitMQProperties rabbitMQProperties;

    @InjectMocks
    private OrderEventPublisher orderEventPublisher;

    @Test
    @DisplayName("publishes OrderCreatedEvent with correct fields")
    void publishOrderCreated_publishesEventWithCorrectFields() {
        // Arrange
        Order order = pendingOrder();
        when(rabbitMQProperties.exchange()).thenReturn("core");
        when(rabbitMQProperties.routingKey()).thenReturn("core-item.created");

        // Act
        orderEventPublisher.publishOrderCreated(order);

        // Assert
        ArgumentCaptor<OrderCreatedEvent> captor = ArgumentCaptor.forClass(OrderCreatedEvent.class);
        verify(rabbitTemplate).convertAndSend(
                org.mockito.ArgumentMatchers.eq("core"),
                org.mockito.ArgumentMatchers.eq("core-item.created"),
                captor.capture()
        );
        OrderCreatedEvent event = captor.getValue();
        assertThat(event.eventId()).isNotNull();
        assertThat(event.occurredAt()).isNotNull();
        assertThat(event.orderId()).isEqualTo(order.id());
        assertThat(event.ownerUserId()).isEqualTo(order.ownerUserId());
        assertThat(event.payload()).contains(order.itemName());
    }
}