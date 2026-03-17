package com.microservices.margo.notification_service.core.infrastructure.listener;

import com.microservices.margo.notification_service.core.application.usecase.StoreNotificationUseCase;
import com.microservices.margo.notification_service.core.application.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedListener {

    private final StoreNotificationUseCase storeNotification;

    @RabbitListener(queues = "${rabbitmq.queue}")
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent {}", event);
        storeNotification.execute(event);
    }
}