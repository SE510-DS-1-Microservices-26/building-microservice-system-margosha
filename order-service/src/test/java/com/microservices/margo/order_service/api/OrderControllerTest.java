package com.microservices.margo.order_service.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.margo.order_service.core.application.request.CreateOrderRequest;
import com.microservices.margo.order_service.core.application.request.UpdateOrderStatusRequest;
import com.microservices.margo.order_service.core.application.usecase.CreateOrderUseCase;
import com.microservices.margo.order_service.core.application.usecase.GetOrderUseCase;
import com.microservices.margo.order_service.core.application.usecase.UpdateOrderStatusUseCase;
import com.microservices.margo.order_service.core.domain.Order;
import com.microservices.margo.order_service.core.domain.OrderStatus;
import com.microservices.margo.order_service.core.infrastructure.config.ObjectMapperConfig;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static com.microservices.margo.order_service.TestData.CUSTOMER_ID;
import static com.microservices.margo.order_service.TestData.ORDER_ID;
import static com.microservices.margo.order_service.TestData.pendingOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("OrderController tests")
@Import(ObjectMapperConfig.class)
@WebMvcTest(OrderController.class)
class OrderControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateOrderUseCase createOrder;

    @MockitoBean
    private GetOrderUseCase getOrder;

    @MockitoBean
    private UpdateOrderStatusUseCase updateStatus;

    private static final String ORDERS_PATH = "/orders";
    private static final String ORDERS_ID_PATH = ORDERS_PATH + "/{id}";
    private static final String ORDERS_ID_PATH_STATUS = ORDERS_ID_PATH + "/status";

    @Test
    void create_shouldReturn201_whenValidRequest() throws Exception {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest(
                "Latte", 2, new BigDecimal("5.99"), CUSTOMER_ID);
        Order order = pendingOrder();

        when(createOrder.execute(request)).thenReturn(order);

        // Act & Assert
        mockMvc.perform(post(ORDERS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(ORDER_ID.toString()))
                .andExpect(jsonPath("$.ownerUserId").value(request.ownerUserId().toString()))
                .andExpect(jsonPath("$.status").value(OrderStatus.PENDING.name()));
    }

    @Test
    void create_shouldReturn400_whenCustomerIdIsNull() throws Exception {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest( "Latte",
                2, new BigDecimal("5.99"), null);

        // Act & Assert
        mockMvc.perform(post(ORDERS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400_whenCustomerIdINotFound() throws Exception {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest(
                "Latte", 2, new BigDecimal("5.99"), CUSTOMER_ID);
        doThrow(new IllegalArgumentException("User not found"))
                .when(createOrder).execute(any());
        // Act & Assert
        mockMvc.perform(post(ORDERS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400_whenQuantityIsLessThanOne() throws Exception {
        // Arrange
        @SuppressWarnings("DataFlowIssue")
        CreateOrderRequest request = new CreateOrderRequest("Latte",
                0, new BigDecimal("5.99"), UUID.randomUUID());

        // Act & Assert
        mockMvc.perform(post(ORDERS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400_whenPriceIsNull() throws Exception {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest("Latte",
                2, null, UUID.randomUUID());

        // Act & Assert
        mockMvc.perform(post(ORDERS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400_whenItemNameIsBlank() throws Exception {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest("",
                2, new BigDecimal("5.99"), UUID.randomUUID());

        // Act & Assert
        mockMvc.perform(post(ORDERS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400_whenPriceIsNegative() throws Exception {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest("Latte",
                2, new BigDecimal("-1.00"), UUID.randomUUID());

        // Act & Assert
        mockMvc.perform(post(ORDERS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400_whenBodyIsMissing() throws Exception {
        // Act & Assert
        mockMvc.perform(post(ORDERS_PATH)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400_whenItemNameIsTooLong() throws Exception {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest(
                "A".repeat(256), 2, new BigDecimal("5.99"), CUSTOMER_ID);

        // Act & Assert
        mockMvc.perform(post(ORDERS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getById_shouldReturn200_whenOrderExists() throws Exception {
        // Arrange
        when(getOrder.execute(ORDER_ID)).thenReturn(pendingOrder());

        // Act & Assert
        mockMvc.perform(get(ORDERS_ID_PATH, ORDER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ORDER_ID.toString()))
                .andExpect(jsonPath("$.ownerUserId").value(CUSTOMER_ID.toString()))
                .andExpect(jsonPath("$.status").value(OrderStatus.PENDING.name()));
    }

    @Test
    void getById_shouldReturn404_whenOrderNotFound() throws Exception {
        // Arrange
        when(getOrder.execute(ORDER_ID)).thenThrow(new EntityNotFoundException("Order not found"));

        // Act & Assert
        mockMvc.perform(get(ORDERS_ID_PATH, ORDER_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void getById_shouldReturn400_whenIdIsNotValidUUID() throws Exception {
        // Act & Assert
        mockMvc.perform(get(ORDERS_ID_PATH, "not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStatus_shouldReturn204_whenValidRequest() throws Exception {
        // Arrange
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.CONFIRMED);

        doNothing().when(updateStatus).execute(ORDER_ID, request);

        // Act & Assert
        mockMvc.perform(patch(ORDERS_ID_PATH_STATUS, ORDER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateStatus_shouldReturn400_whenStatusIsNull() throws Exception {
        // Arrange
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(null);

        // Act & Assert
        mockMvc.perform(patch(ORDERS_ID_PATH_STATUS, ORDER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStatus_shouldReturn400_whenInvalidTransition() throws Exception {
        // Arrange
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.DELIVERED);

        doThrow(new IllegalStateException("Cannot transition order from PENDING to DELIVERED"))
                .when(updateStatus).execute(ORDER_ID, request);

        // Act & Assert
        mockMvc.perform(patch(ORDERS_ID_PATH_STATUS, ORDER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStatus_shouldReturn404_whenOrderNotFound() throws Exception {
        // Arrange
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.CONFIRMED);

        doThrow(new EntityNotFoundException("Order not found: " + ORDER_ID))
                .when(updateStatus).execute(ORDER_ID, request);

        // Act & Assert
        mockMvc.perform(patch(ORDERS_ID_PATH_STATUS, ORDER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateStatus_shouldReturn400_whenBodyIsMissing() throws Exception {
        // Act & Assert
        mockMvc.perform(patch(ORDERS_ID_PATH_STATUS, ORDER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}