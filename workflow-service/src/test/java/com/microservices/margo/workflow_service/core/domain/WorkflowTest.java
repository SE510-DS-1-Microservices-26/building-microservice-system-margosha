package com.microservices.margo.workflow_service.core.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDateTime;

import static com.microservices.margo.workflow_service.TestData.buildWorkflow;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Workflow domain tests")
class WorkflowTest {

    @ParameterizedTest(name = "transitionTo({0}) sets state correctly")
    @EnumSource(WorkflowState.class)
    @DisplayName("transitionTo sets the given state and updates updatedAt")
    void transitionTo_setsStateAndUpdatesTimestamp(WorkflowState target) {
        // Arrange
        Workflow workflow = buildWorkflow(WorkflowState.STARTED);
        LocalDateTime before = workflow.updatedAt();

        // Act
        Workflow result = workflow.transitionTo(target);

        // Assert
        assertThat(result.state()).isEqualTo(target);
        assertThat(result.updatedAt()).isAfterOrEqualTo(before);
    }

    @Test
    @DisplayName("fail sets state to FAILED and records lastError")
    void fail_setsFailedStateAndError() {
        // Arrange
        Workflow workflow = buildWorkflow(WorkflowState.ORDER_CREATED);

        // Act
        Workflow result = workflow.fail("something went wrong");

        // Assert
        assertThat(result.state()).isEqualTo(WorkflowState.FAILED);
        assertThat(result.lastError()).isEqualTo("something went wrong");
    }

    @Test
    @DisplayName("compensating sets state to COMPENSATING and records lastError")
    void compensating_setsCompensatingStateAndError() {
        // Arrange
        Workflow workflow = buildWorkflow(WorkflowState.ORDER_CREATED);

        // Act
        Workflow result = workflow.compensating("payment failed");

        // Assert
        assertThat(result.state()).isEqualTo(WorkflowState.COMPENSATING);
        assertThat(result.lastError()).isEqualTo("payment failed");
    }

    @Test
    @DisplayName("transitionTo preserves workflowId and type")
    void transitionTo_preservesImmutableFields() {
        // Arrange
        Workflow workflow = buildWorkflow(WorkflowState.STARTED);

        // Act
        Workflow result = workflow.transitionTo(WorkflowState.COMPLETED);

        // Assert
        assertThat(result.id()).isEqualTo(workflow.id());
        assertThat(result.type()).isEqualTo(workflow.type());
    }
}