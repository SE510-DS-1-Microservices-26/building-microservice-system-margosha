package com.microservices.margo.workflow_service;

import com.microservices.margo.workflow_service.core.application.request.CreateOrderRequest;
import com.microservices.margo.workflow_service.core.domain.Workflow;
import com.microservices.margo.workflow_service.core.domain.WorkflowState;
import com.microservices.margo.workflow_service.core.domain.WorkflowType;
import com.microservices.margo.workflow_service.core.infrastructure.entity.WorkflowEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public final class TestData {

    public static final UUID WORKFLOW_ID = UUID.randomUUID();
    public static final UUID OWNER_ID = UUID.randomUUID();
    public static final UUID ORDER_ID = UUID.randomUUID();
    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String MDC_KEY = "correlationId";

    public static CreateOrderRequest buildRequest() {
        return new CreateOrderRequest(OWNER_ID, "Latte", 2, BigDecimal.valueOf(5.99));
    }

    public static Workflow buildWorkflow(WorkflowState state) {
        return Workflow.builder()
                .id(WORKFLOW_ID)
                .type(WorkflowType.CREATE_ORDER.toString())
                .state(state)
                .payload("test-payload")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static WorkflowEntity buildEntity(WorkflowState state) {
        return WorkflowEntity.builder()
                .id(WORKFLOW_ID)
                .type(WorkflowType.CREATE_ORDER.toString())
                .state(state)
                .payload("test-payload")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private TestData() {}
}