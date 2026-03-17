package com.microservices.margo.order_service.core.application.usecase;

import com.microservices.margo.order_service.core.application.mapper.OrderMapper;
import com.microservices.margo.order_service.core.application.request.CreateOrderRequest;
import com.microservices.margo.order_service.core.domain.Order;
import com.microservices.margo.order_service.core.infrastructure.client.UserValidationClient;
import com.microservices.margo.order_service.core.infrastructure.entity.OrderEntity;
import com.microservices.margo.order_service.core.infrastructure.publisher.OrderEventPublisher;
import com.microservices.margo.order_service.core.infrastructure.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.UUID;

import static com.microservices.margo.order_service.TestData.buildEntity;
import static com.microservices.margo.order_service.TestData.buildOrder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("CreateOrderUseCase tests")
@ExtendWith(MockitoExtension.class)
class CreateOrderUseCaseTest {

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserValidationClient userValidationClient;

    @Mock
    private OrderEventPublisher eventPublisher;

    @InjectMocks
    private CreateOrderUseCase createOrderUseCase;

    @Test
    void execute_shouldSaveOrderAndPublishEventWhenUserFound() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest("Laptop", 1, new BigDecimal("1499.99"), UUID.randomUUID());
        OrderEntity entity = buildEntity(UUID.randomUUID());
        OrderEntity savedEntity = buildEntity(entity.getId());
        Order expectedOrder = buildOrder(entity.getId());

        when(orderMapper.toEntity(request)).thenReturn(entity);
        when(orderRepository.save(entity)).thenReturn(savedEntity);
        when(orderMapper.toDomain(savedEntity)).thenReturn(expectedOrder);

        // Act
        Order result = createOrderUseCase.execute(request);

        // Assert
        assertThat(result).isEqualTo(expectedOrder);
        verify(orderRepository, times(1)).save(entity);
        verify(eventPublisher).publishOrderCreated(expectedOrder);
    }

    @Test
    void execute_shouldThrowAndNotSaveWhenUserNotFound() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest("Laptop", 1, new BigDecimal("1499.99"), UUID.randomUUID());
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found"))
                .when(userValidationClient).validateUserExists(request.ownerUserId());

        // Act & Assert
        assertThatThrownBy(() -> createOrderUseCase.execute(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User not found");

        verify(orderRepository, never()).save(any());
        verify(eventPublisher, never()).publishOrderCreated(any());
    }
}