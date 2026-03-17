package com.microservices.margo.order_service.core.application.usecase;

import com.microservices.margo.order_service.core.application.mapper.OrderMapper;
import com.microservices.margo.order_service.core.domain.Order;
import com.microservices.margo.order_service.core.infrastructure.entity.OrderEntity;
import com.microservices.margo.order_service.core.infrastructure.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import java.util.UUID;
import static com.microservices.margo.order_service.TestData.buildEntity;
import static com.microservices.margo.order_service.TestData.buildOrder;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetOrderUseCaseTest {

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private GetOrderUseCase getOrderUseCase;

    @Test
    void execute_shouldReturnOrder_whenExists() {
        // Arrange
        UUID id = UUID.randomUUID();
        OrderEntity entity = buildEntity(id);
        Order expected = buildOrder(id);

        when(orderRepository.findById(id)).thenReturn(Optional.of(entity));
        when(orderMapper.toDomain(entity)).thenReturn(expected);

        // Act
        Order result = getOrderUseCase.execute(id);

        // Assert
        assertThat(result).isEqualTo(expected);
        verify(orderRepository).findById(id);
        verify(orderMapper).toDomain(entity);
    }

    @Test
    void execute_shouldThrowEntityNotFoundException_whenNotFound() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(orderRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> getOrderUseCase.execute(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(id.toString());

        verify(orderRepository).findById(id);
        verifyNoInteractions(orderMapper);
    }
}