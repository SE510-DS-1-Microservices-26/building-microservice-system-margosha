package com.microservices.margo.order_service.core.application.usecase;

import com.microservices.margo.order_service.core.application.mapper.OrderMapper;
import com.microservices.margo.order_service.core.application.request.UpdateOrderStatusRequest;
import com.microservices.margo.order_service.core.domain.Order;
import com.microservices.margo.order_service.core.domain.OrderStatus;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateOrderStatusUseCaseTest {

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private UpdateOrderStatusUseCase updateOrderStatusUseCase;

    @Test
    void execute_shouldUpdateStatus_fromPendingToConfirmed() {
        // Arrange
        UUID id = UUID.randomUUID();
        Order pendingOrder = buildOrder(id, OrderStatus.PENDING);
        OrderEntity entity = buildEntity(id, OrderStatus.PENDING);
        OrderEntity updatedEntity = buildEntity(id, OrderStatus.CONFIRMED);
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.CONFIRMED);

        when(orderRepository.findById(id)).thenReturn(Optional.of(entity));
        when(orderMapper.toDomain(entity)).thenReturn(pendingOrder);
        when(orderMapper.toEntity(any(Order.class))).thenReturn(updatedEntity);

        // Act
        updateOrderStatusUseCase.execute(id, request);

        // Assert
        verify(orderRepository).findById(id);
        verify(orderRepository).save(updatedEntity);
    }

    @Test
    void execute_shouldUpdateStatus_fromPendingToCancelled() {
        // Arrange
        UUID id = UUID.randomUUID();
        Order pendingOrder = buildOrder(id, OrderStatus.PENDING);
        OrderEntity entity = buildEntity(id, OrderStatus.PENDING);
        OrderEntity cancelledEntity = buildEntity(id, OrderStatus.CANCELLED);
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.CANCELLED);

        when(orderRepository.findById(id)).thenReturn(Optional.of(entity));
        when(orderMapper.toDomain(entity)).thenReturn(pendingOrder);
        when(orderMapper.toEntity(any(Order.class))).thenReturn(cancelledEntity);

        // Act
        updateOrderStatusUseCase.execute(id, request);

        // Assert
        verify(orderRepository).save(cancelledEntity);
    }

    @Test
    void execute_shouldUpdateStatus_fromConfirmedToDelivered() {
        // Arrange
        UUID id = UUID.randomUUID();
        Order confirmedOrder = buildOrder(id, OrderStatus.CONFIRMED);
        OrderEntity entity = buildEntity(id, OrderStatus.CONFIRMED);
        OrderEntity deliveredEntity = buildEntity(id, OrderStatus.DELIVERED);
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.DELIVERED);

        when(orderRepository.findById(id)).thenReturn(Optional.of(entity));
        when(orderMapper.toDomain(entity)).thenReturn(confirmedOrder);
        when(orderMapper.toEntity(any(Order.class))).thenReturn(deliveredEntity);

        // Act
        updateOrderStatusUseCase.execute(id, request);

        // Assert
        verify(orderRepository).save(deliveredEntity);
    }

    @Test
    void execute_shouldThrowEntityNotFoundException_whenOrderNotFound() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(orderRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() ->
                updateOrderStatusUseCase.execute(id, new UpdateOrderStatusRequest(OrderStatus.CONFIRMED)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(id.toString());

        verify(orderRepository, never()).save(any());
    }

    @Test
    void execute_shouldThrowIllegalStateException_whenTransitionFromDeliveredIsAttempted() {
        // Arrange
        UUID id = UUID.randomUUID();
        Order deliveredOrder = buildOrder(id, OrderStatus.DELIVERED);
        OrderEntity entity = buildEntity(id, OrderStatus.DELIVERED);

        when(orderRepository.findById(id)).thenReturn(Optional.of(entity));
        when(orderMapper.toDomain(entity)).thenReturn(deliveredOrder);

        // Act & Assert
        assertThatThrownBy(() ->
                updateOrderStatusUseCase.execute(id, new UpdateOrderStatusRequest(OrderStatus.CONFIRMED)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(OrderStatus.DELIVERED.name());

        verify(orderRepository, never()).save(any());
    }

    @Test
    void execute_shouldThrowIllegalStateException_whenTransitionFromCancelledIsAttempted() {
        // Arrange
        UUID id = UUID.randomUUID();
        Order cancelledOrder = buildOrder(id, OrderStatus.CANCELLED);
        OrderEntity entity = buildEntity(id, OrderStatus.CANCELLED);

        when(orderRepository.findById(id)).thenReturn(Optional.of(entity));
        when(orderMapper.toDomain(entity)).thenReturn(cancelledOrder);

        // Act & Assert
        assertThatThrownBy(() ->
                updateOrderStatusUseCase.execute(id, new UpdateOrderStatusRequest(OrderStatus.CONFIRMED)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(OrderStatus.CANCELLED.name());

        verify(orderRepository, never()).save(any());
    }

    @Test
    void execute_shouldThrowIllegalStateException_whenMovingPendingToDelivered() {
        // Arrange
        UUID id = UUID.randomUUID();
        Order pendingOrder = buildOrder(id, OrderStatus.PENDING);
        OrderEntity entity = buildEntity(id, OrderStatus.PENDING);

        when(orderRepository.findById(id)).thenReturn(Optional.of(entity));
        when(orderMapper.toDomain(entity)).thenReturn(pendingOrder);

        // Act & Assert
        assertThatThrownBy(() ->
                updateOrderStatusUseCase.execute(id, new UpdateOrderStatusRequest(OrderStatus.DELIVERED)))
                .isInstanceOf(IllegalStateException.class);

        verify(orderRepository, never()).save(any());
    }
}