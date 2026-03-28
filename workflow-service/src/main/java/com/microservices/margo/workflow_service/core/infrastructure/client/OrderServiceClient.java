package com.microservices.margo.workflow_service.core.infrastructure.client;

import com.microservices.margo.workflow_service.core.application.request.CreateOrderRequest;
import com.microservices.margo.workflow_service.core.domain.OrderStatus;
import com.microservices.margo.workflow_service.core.infrastructure.config.OrderServiceProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class OrderServiceClient {

    private final RestClient restClient;
    private final OrderServiceProperties.Url url;
    private final OrderServiceProperties.Params params;

    public OrderServiceClient(RestClient restClient, OrderServiceProperties orderServiceProperties) {
        this.restClient = restClient;
        this.url = orderServiceProperties.getUrl();
        this.params = orderServiceProperties.getParams();
    }

    public UUID createOrder(CreateOrderRequest request) {
        Map<?, ?> response = restClient.post()
                .uri(url.base() + url.createOrder())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        params.ownerUserId(), request.ownerUserId(),
                        params.itemName(), request.itemName(),
                        params.quantity(), request.quantity(),
                        params.price(), request.price()
                ))
                .retrieve()
                .body(Map.class);

        if (response == null){
            log.error("Received null response from order service.");
            throw new RuntimeException("Failed to create order:(");
        }

        return UUID.fromString(response.get(params.id()).toString());
    }

    public void confirmOrder(UUID orderId) {
        restClient.patch()
                .uri(url.base() + url.changeStatus(), orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(params.newStatus(), OrderStatus.CONFIRMED))
                .retrieve()
                .toBodilessEntity();
        log.info("Order {} confirmed", orderId);
    }

    public void cancelOrder(UUID orderId) {
        restClient.patch()
                .uri(url.base() + url.changeStatus(), orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(params.newStatus(), OrderStatus.CANCELLED))
                .retrieve()
                .toBodilessEntity();
        log.info("Compensation: order {} cancelled", orderId);
    }
}