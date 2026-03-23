package com.microservices.margo.workflow_service.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.margo.workflow_service.api.exception.GlobalExceptionHandler;
import com.microservices.margo.workflow_service.core.application.request.CreateOrderRequest;
import com.microservices.margo.workflow_service.core.application.usecase.GetWorkflowUseCase;
import com.microservices.margo.workflow_service.core.application.usecase.StartCreateOrderWorkflowUseCase;
import com.microservices.margo.workflow_service.core.domain.Workflow;
import com.microservices.margo.workflow_service.core.domain.WorkflowState;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static com.microservices.margo.workflow_service.TestData.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {WorkflowController.class, GlobalExceptionHandler.class})
@DisplayName("WorkflowController tests")
class WorkflowControllerTest {
    private static final String WORKFLOWS_PATH = "/workflows";
    private static final String PLACE_ORDER_URL = WORKFLOWS_PATH + "/create-order";
    private static final String GET_BY_ID_URL = WORKFLOWS_PATH + "/{workflowId}";

    @Autowired
    MockMvc mockMvc;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private StartCreateOrderWorkflowUseCase startPlaceOrderWorkflow;

    @MockitoBean
    private GetWorkflowUseCase getWorkflow;

    @Test
    @DisplayName("POST /workflows/create-order returns 202 with workflow")
    void startCreateOrder_returns202_withWorkflow() throws Exception {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest(OWNER_ID, "Latte", 2, BigDecimal.valueOf(5.99));
        Workflow workflow = buildWorkflow(WorkflowState.COMPLETED);
        when(startPlaceOrderWorkflow.execute(any())).thenReturn(workflow);

        // Act & Assert
        mockMvc.perform(post(PLACE_ORDER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value(WORKFLOW_ID.toString()))
                .andExpect(jsonPath("$.state").value(WorkflowState.COMPLETED.toString()));
    }

    @Test
    @DisplayName("POST /workflows/create-order returns 400 when request is invalid")
    void startCreateOrder_returns400_whenRequestInvalid() throws Exception {
        // Arrange
        String invalidBody = """
                {"ownerUserId": null, "itemName": "", "quantity": 0, "price": -1}
                """;

        // Act & Assert
        mockMvc.perform(post(PLACE_ORDER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /workflows/{workflowId} returns 200 with workflow")
    void getById_returns200_withWorkflow() throws Exception {
        // Arrange
        Workflow workflow = buildWorkflow(WorkflowState.COMPLETED);
        when(getWorkflow.execute(WORKFLOW_ID)).thenReturn(workflow);

        // Act & Assert
        mockMvc.perform(get(GET_BY_ID_URL, WORKFLOW_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(WORKFLOW_ID.toString()))
                .andExpect(jsonPath("$.state").value(WorkflowState.COMPLETED.toString()));
    }

    @Test
    @DisplayName("GET /workflows/{workflowId} returns 404 when not found")
    void getById_returns404_whenNotFound() throws Exception {
        // Arrange
        UUID unknown = UUID.randomUUID();
        when(getWorkflow.execute(unknown)).thenThrow(new EntityNotFoundException("Workflow not found: " + unknown));

        // Act & Assert
        mockMvc.perform(get(GET_BY_ID_URL, unknown))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Workflow not found: " + unknown));
    }

    @Test
    @DisplayName("GET /workflows/{workflowId} returns 400 when workflowId is not a UUID")
    void getById_returns400_whenWorkflowIdIsInvalid() throws Exception {
        // Act & Assert
        mockMvc.perform(get(GET_BY_ID_URL, "aaaa"))
                .andExpect(status().isBadRequest());
    }
}