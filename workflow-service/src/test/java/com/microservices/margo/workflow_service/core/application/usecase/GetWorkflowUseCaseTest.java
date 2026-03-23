package com.microservices.margo.workflow_service.core.application.usecase;

import com.microservices.margo.workflow_service.core.application.mapper.WorkflowMapper;
import com.microservices.margo.workflow_service.core.domain.Workflow;
import com.microservices.margo.workflow_service.core.domain.WorkflowState;
import com.microservices.margo.workflow_service.core.infrastructure.entity.WorkflowEntity;
import com.microservices.margo.workflow_service.core.infrastructure.repository.WorkflowRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static com.microservices.margo.workflow_service.TestData.WORKFLOW_ID;
import static com.microservices.margo.workflow_service.TestData.buildEntity;
import static com.microservices.margo.workflow_service.TestData.buildWorkflow;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetWorkflowUseCase tests")
class GetWorkflowUseCaseTest {

    @Mock
    private WorkflowRepository repository;

    @Mock
    private WorkflowMapper mapper;

    @InjectMocks
    private GetWorkflowUseCase useCase;

    @Test
    @DisplayName("returns workflow when found")
    void execute_returnsWorkflow_whenFound() {
        // Arrange
        WorkflowEntity entity = buildEntity(WorkflowState.COMPLETED);
        Workflow domain = buildWorkflow(WorkflowState.COMPLETED);
        when(repository.findById(WORKFLOW_ID)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        // Act
        Workflow result = useCase.execute(WORKFLOW_ID);

        // Assert
        assertThat(result.id()).isEqualTo(WORKFLOW_ID);
        assertThat(result.state()).isEqualTo(WorkflowState.COMPLETED);
    }

    @Test
    @DisplayName("throws EntityNotFoundException when not found")
    void execute_throwsEntityNotFoundException_whenNotFound() {
        // Arrange
        UUID unknown = UUID.randomUUID();
        when(repository.findById(unknown)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(unknown))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(unknown.toString());
    }
}