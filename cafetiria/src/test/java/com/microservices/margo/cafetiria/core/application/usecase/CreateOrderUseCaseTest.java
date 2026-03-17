package com.microservices.margo.cafetiria.core.application.usecase;

import com.microservices.margo.cafetiria.core.application.mapper.OrderMapper;
import com.microservices.margo.cafetiria.core.application.request.CreateOrderRequest;
import com.microservices.margo.cafetiria.core.domain.Order;
import com.microservices.margo.cafetiria.core.infrastructure.entity.OrderEntity;
import com.microservices.margo.cafetiria.core.infrastructure.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.UUID;
import static com.microservices.margo.cafetiria.TestData.buildEntity;
import static com.microservices.margo.cafetiria.TestData.buildOrder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("CreateOrderUseCase tests")
@ExtendWith(MockitoExtension.class)
class CreateOrderUseCaseTest {

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private CreateOrderUseCase createOrderUseCase;

    @Test
    void execute_shouldSaveOrderAndReturnDomain() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest("Alice", "Laptop",
                1, new BigDecimal("1499.99"));

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
        verify(orderMapper).toEntity(request);
        verify(orderRepository, times(1)).save(entity);
        verify(orderMapper).toDomain(savedEntity);
    }
}