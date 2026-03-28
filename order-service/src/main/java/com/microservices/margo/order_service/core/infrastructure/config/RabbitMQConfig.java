package com.microservices.margo.order_service.core.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RabbitMQConfig {
    private final RabbitMQProperties rabbitMQProperties;

    @Bean
    public TopicExchange coreExchange() {
        return new TopicExchange(rabbitMQProperties.exchange());
    }

    @Bean
    public Queue orderCreatedQueue() {
        return QueueBuilder.durable(rabbitMQProperties.queue()).build();
    }

    @Bean
    public Binding orderCreatedBinding(Queue orderCreatedQueue, TopicExchange coreExchange) {
        return BindingBuilder.bind(orderCreatedQueue)
                .to(coreExchange)
                .with(rabbitMQProperties.routingKey());
    }

    @Bean
    public JacksonJsonMessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
