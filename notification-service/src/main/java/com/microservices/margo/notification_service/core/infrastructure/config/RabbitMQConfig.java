package com.microservices.margo.notification_service.core.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;

@Configuration
@RequiredArgsConstructor
public class RabbitMQConfig {
    private final RabbitMQProperties properties;

    @Bean
    public TopicExchange coreExchange() {
        return new TopicExchange(properties.exchange());
    }

    @Bean
    public Queue orderCreatedQueue() {
        return QueueBuilder.durable(properties.queue()).build();
    }

    @Bean
    public Binding orderCreatedBinding(Queue orderCreatedQueue, TopicExchange coreExchange) {
        return BindingBuilder.bind(orderCreatedQueue)
                .to(coreExchange)
                .with(properties.routingKey());
    }

    @Bean
    public JacksonJsonMessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}