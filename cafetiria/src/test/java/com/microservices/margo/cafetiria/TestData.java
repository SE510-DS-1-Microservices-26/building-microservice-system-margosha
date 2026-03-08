package com.microservices.margo.cafetiria;

import com.microservices.margo.cafetiria.core.domain.Order;
import com.microservices.margo.cafetiria.core.domain.OrderStatus;
import com.microservices.margo.cafetiria.core.infrastructure.entity.OrderEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class TestData {
    public static final UUID ORDER_ID = UUID.randomUUID();
    public static final String CUSTOMER_NAME = "Boba";

    public static OrderEntity buildEntity(UUID id) {
        return buildEntity(id, OrderStatus.PENDING);
    }

    public static Order buildOrder(UUID id) {
        return buildOrder(id, OrderStatus.PENDING);
    }

    public static Order buildOrder(UUID id, OrderStatus status) {
        return Order.builder()
                .id(id)
                .customerName(CUSTOMER_NAME)
                .itemName("Laptop")
                .quantity(1)
                .price(new BigDecimal("1499.99"))
                .status(status)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static OrderEntity buildEntity(UUID id, OrderStatus status) {
        OrderEntity e = new OrderEntity();
        e.setId(id);
        e.setCustomerName(CUSTOMER_NAME);
        e.setItemName("Laptop");
        e.setQuantity(1);
        e.setPrice(new BigDecimal("1499.99"));
        e.setStatus(status);
        return e;
    }

    public static Order pendingOrder() {
        return Order.builder()
                .id(ORDER_ID)
                .customerName(CUSTOMER_NAME)
                .itemName("Latte")
                .quantity(2)
                .price(new BigDecimal("5.99"))
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }
}