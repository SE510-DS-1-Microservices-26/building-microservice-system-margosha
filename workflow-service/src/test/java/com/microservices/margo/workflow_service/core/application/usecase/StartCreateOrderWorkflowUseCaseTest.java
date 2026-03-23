package com.microservices.margo.workflow_service.core.application.usecase;

import com.microservices.margo.workflow_service.core.application.mapper.WorkflowMapper;
import com.microservices.margo.workflow_service.core.application.request.CreateOrderRequest;
import com.microservices.margo.workflow_service.core.domain.Workflow;
import com.microservices.margo.workflow_service.core.domain.WorkflowState;
import com.microservices.margo.workflow_service.core.infrastructure.client.OrderServiceClient;
import com.microservices.margo.workflow_service.core.infrastructure.entity.WorkflowEntity;
import com.microservices.margo.workflow_service.core.infrastructure.repository.WorkflowRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.microservices.margo.workflow_service.TestData.ORDER_ID;
import static com.microservices.margo.workflow_service.TestData.buildEntity;
import static com.microservices.margo.workflow_service.TestData.buildRequest;
import static com.microservices.margo.workflow_service.TestData.buildWorkflow;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StartCreateOrderWorkflowUseCase tests")
class StartCreateOrderWorkflowUseCaseTest {

    @Mock
    private WorkflowRepository repository;

    @Mock
    private WorkflowMapper mapper;

    @Mock
    private OrderServiceClient orderServiceClient;

    @InjectMocks
    private StartCreateOrderWorkflowUseCase useCase;

    private void stubMapperRoundTrip() {
        when(mapper.toEntity(any(Workflow.class)))
                .thenAnswer(inv -> {
                    Workflow w = inv.getArgument(0);
                    return buildEntity(w.state());
                });
        when(repository.save(any(WorkflowEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toDomain(any(WorkflowEntity.class)))
                .thenAnswer(inv -> {
                    WorkflowEntity e = inv.getArgument(0);
                    return buildWorkflow(e.getState());
                });
    }

    @Test
    @DisplayName("returns COMPLETED when both steps succeed")
    void execute_returnsCompleted_whenAllStepsSucceed() {
        // Arrange
        stubMapperRoundTrip();
        when(orderServiceClient.createOrder(any())).thenReturn(ORDER_ID);
        doNothing().when(orderServiceClient).confirmOrder(ORDER_ID);
        CreateOrderRequest request = buildRequest();

        // Act
        Workflow result = useCase.execute(request);

        // Assert
        assertThat(result.state()).isEqualTo(WorkflowState.COMPLETED);
        verify(orderServiceClient).createOrder(request);
        verify(orderServiceClient).confirmOrder(ORDER_ID);
    }

    @Test
    @DisplayName("returns FAILED when order creation fails")
    void execute_returnsFailed_whenOrderCreationFails() {
        // Arrange
        stubMapperRoundTrip();
        when(orderServiceClient.createOrder(any())).thenThrow(new RuntimeException("order-service down"));
        CreateOrderRequest request = buildRequest();

        // Act
        Workflow result = useCase.execute(request);

        // Assert
        assertThat(result.state()).isEqualTo(WorkflowState.FAILED);
        verify(orderServiceClient, never()).confirmOrder(any());
        verify(orderServiceClient, never()).cancelOrder(any());
    }

    @Test
    @DisplayName("returns COMPENSATED when confirmation fails and cancellation succeeds")
    void execute_returnsCompensated_whenConfirmFailsAndCancelSucceeds() {
        // Arrange
        stubMapperRoundTrip();
        when(orderServiceClient.createOrder(any())).thenReturn(ORDER_ID);
        doThrow(new RuntimeException("confirm rejected")).when(orderServiceClient).confirmOrder(ORDER_ID);
        doNothing().when(orderServiceClient).cancelOrder(ORDER_ID);
        CreateOrderRequest request = buildRequest();

        // Act
        Workflow result = useCase.execute(request);

        // Assert
        assertThat(result.state()).isEqualTo(WorkflowState.COMPENSATED);
        verify(orderServiceClient).cancelOrder(ORDER_ID);
    }

    @Test
    @DisplayName("returns FAILED when confirmation fails and compensation also fails")
    void execute_returnsFailed_whenConfirmAndCompensationBothFail() {
        // Arrange
        stubMapperRoundTrip();
        when(orderServiceClient.createOrder(any())).thenReturn(ORDER_ID);
        doThrow(new RuntimeException("confirm rejected")).when(orderServiceClient).confirmOrder(ORDER_ID);
        doThrow(new RuntimeException("cancel also failed")).when(orderServiceClient).cancelOrder(ORDER_ID);
        CreateOrderRequest request = buildRequest();

        // Act
        Workflow result = useCase.execute(request);

        // Assert
        assertThat(result.state()).isEqualTo(WorkflowState.FAILED);
    }
}