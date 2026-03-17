package com.microservices.margo.cafetiria.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.margo.cafetiria.core.application.request.CreateOrderRequest;
import com.microservices.margo.cafetiria.core.application.request.UpdateOrderStatusRequest;
import com.microservices.margo.cafetiria.core.application.usecase.CreateOrderUseCase;
import com.microservices.margo.cafetiria.core.application.usecase.GetOrderUseCase;
import com.microservices.margo.cafetiria.core.application.usecase.UpdateOrderStatusUseCase;
import com.microservices.margo.cafetiria.core.domain.Order;
import com.microservices.margo.cafetiria.core.domain.OrderStatus;
import com.microservices.margo.cafetiria.core.infrastructure.config.ObjectMapperConfig;
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
import java.time.LocalDateTime;
import java.util.UUID;
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

    private static final UUID ORDER_ID = UUID.randomUUID();
    private static final String ORDERS_PATH = "/orders";
    private static final String ORDERS_ID_PATH = ORDERS_PATH + "/{id}";
    private static final String ORDERS_ID_PATH_STATUS = ORDERS_ID_PATH + "/status";

    private Order sampleOrder() {
        return Order.builder()
                .id(ORDER_ID)
                .customerName("John Doe")
                .itemName("Latte")
                .quantity(2)
                .price(new BigDecimal("5.99"))
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void create_shouldReturn201_whenValidRequest() throws Exception {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest("John Doe",
                "Latte", 2, new BigDecimal("5.99"));
        Order order = sampleOrder();

        when(createOrder.execute(request)).thenReturn(order);

        // Act & Assert
        mockMvc.perform(post(ORDERS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(ORDER_ID.toString()))
                .andExpect(jsonPath("$.customerName").value(request.customerName()))
                .andExpect(jsonPath("$.status").value(OrderStatus.PENDING.name()));
    }

    @Test
    void create_shouldReturn400_whenCustomerNameIsBlank() throws Exception {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest("", "Latte",
                2, new BigDecimal("5.99"));

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
        CreateOrderRequest request = new CreateOrderRequest("John Doe", "Latte",
                0, new BigDecimal("5.99"));

        // Act & Assert
        mockMvc.perform(post(ORDERS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400_whenPriceIsNull() throws Exception {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest("John Doe", "Latte",
                2, null);

        // Act & Assert
        mockMvc.perform(post(ORDERS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400_whenItemNameIsBlank() throws Exception {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest("John Doe", "",
                2, new BigDecimal("5.99"));

        // Act & Assert
        mockMvc.perform(post(ORDERS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400_whenPriceIsNegative() throws Exception {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest("John Doe", "Latte",
                2, new BigDecimal("-1.00"));

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
               "Alice", "A".repeat(256), 2, new BigDecimal("5.99"));

        // Act & Assert
        mockMvc.perform(post(ORDERS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400_whenCustomerNameIsTooLong() throws Exception {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest(
                "A".repeat(256), "late", 2, new BigDecimal("5.99"));

        // Act & Assert
        mockMvc.perform(post(ORDERS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getById_shouldReturn200_whenOrderExists() throws Exception {
        // Arrange
        when(getOrder.execute(ORDER_ID)).thenReturn(sampleOrder());

        // Act & Assert
        mockMvc.perform(get(ORDERS_ID_PATH, ORDER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ORDER_ID.toString()))
                .andExpect(jsonPath("$.customerName").value(sampleOrder().customerName()))
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