package com.microservices.margo.cafetiria.core.application.usecase;

import com.microservices.margo.cafetiria.core.application.mapper.OrderMapper;
import com.microservices.margo.cafetiria.core.domain.Order;
import com.microservices.margo.cafetiria.core.infrastructure.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetOrderUseCase {
    private final OrderMapper orderMapper;
    private final OrderRepository orderRepository;

    public Order execute(UUID id) {
        return orderRepository.findById(id)
                .map(orderMapper::toDomain)
                .orElseThrow(() -> new EntityNotFoundException("Order with id: " + id + " was not found!"));
    }
}
