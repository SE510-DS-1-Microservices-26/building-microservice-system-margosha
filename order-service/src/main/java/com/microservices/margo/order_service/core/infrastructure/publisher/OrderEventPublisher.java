package com.microservices.margo.order_service.core.infrastructure.publisher;

import com.microservices.margo.order_service.core.domain.Order;
import com.microservices.margo.order_service.core.infrastructure.config.RabbitMQProperties;
import com.microservices.margo.order_service.core.application.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQProperties rabbitMQProperties;

    public void publishOrderCreated(Order order) {
        OrderCreatedEvent event = new OrderCreatedEvent(
                UUID.randomUUID(),
                Instant.now(),
                UUID.randomUUID(),
                order.id(),
                order.ownerUserId(),
                "Order created: %s x%d @ %s".formatted(order.itemName(), order.quantity(), order.price())
        );
        rabbitTemplate.convertAndSend(rabbitMQProperties.exchange(), rabbitMQProperties.routingKey(), event);
        log.info("Published OrderCreatedEvent for orderId={}", order.id());
    }
}