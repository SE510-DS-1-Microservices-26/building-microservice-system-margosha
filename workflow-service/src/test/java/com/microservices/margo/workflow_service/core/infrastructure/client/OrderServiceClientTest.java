package com.microservices.margo.workflow_service.core.infrastructure.client;

import com.microservices.margo.workflow_service.core.application.request.CreateOrderRequest;
import com.microservices.margo.workflow_service.core.domain.OrderStatus;
import com.microservices.margo.workflow_service.core.infrastructure.config.OrderServiceProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static com.microservices.margo.workflow_service.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("OrderServiceClient tests")
class OrderServiceClientTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private OrderServiceClient client;

    private static final String BASE_URL = "http://order-service:8088";
    private static final String CREATE_PATH = "/api/orders";
    private static final String STATUS_PATH = "/api/orders/status/";

    @BeforeEach
    void setUp() {
        OrderServiceProperties.Url url = new OrderServiceProperties.Url(BASE_URL, CREATE_PATH, STATUS_PATH);
        OrderServiceProperties.Params params = new OrderServiceProperties.Params(
                "ownerUserId", "itemName", "quantity", "price", "newStatus", "id");

        client = new OrderServiceClient(restClient, new OrderServiceProperties(url, params));

        doReturn(requestSpec).when(requestSpec).uri(anyString());
        doReturn(requestSpec).when(requestSpec).uri(anyString(), any(Object[].class));
        doReturn(requestSpec).when(requestSpec).contentType(any());
        doReturn(requestSpec).when(requestSpec).body(any(Object.class));
        doReturn(responseSpec).when(requestSpec).retrieve();
    }

    @Test
    @DisplayName("createOrder returns UUID from response body")
    void createOrder_returnsUUID_onSuccess() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest(OWNER_ID, "Latte", 2, BigDecimal.valueOf(5.99));
        when(restClient.post()).thenReturn(requestSpec);
        when(responseSpec.body(Map.class)).thenReturn(Map.of("id", ORDER_ID.toString()));

        // Act
        UUID result = client.createOrder(request);

        // Assert
        assertThat(result).isEqualTo(ORDER_ID);
    }

    @Test
    @DisplayName("createOrder throws RuntimeException when response is null")
    void createOrder_throwsRuntimeException_whenResponseIsNull() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest(OWNER_ID, "Latte", 2, BigDecimal.valueOf(5.99));
        when(restClient.post()).thenReturn(requestSpec);
        when(responseSpec.body(Map.class)).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> client.createOrder(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to create order");
    }

    @Test
    @DisplayName("confirmOrder sends CONFIRMED status")
    void confirmOrder_sendsConfirmedStatus() {
        // Arrange
        when(restClient.patch()).thenReturn(requestSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(ResponseEntity.ok().build());

        // Act
        client.confirmOrder(ORDER_ID);

        // Assert
        verify(requestSpec).body(Map.of("newStatus", OrderStatus.CONFIRMED));
        verify(responseSpec).toBodilessEntity();
    }

    @Test
    @DisplayName("cancelOrder sends CANCELLED status")
    void cancelOrder_sendsCancelledStatus() {
        // Arrange
        when(restClient.patch()).thenReturn(requestSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(ResponseEntity.ok().build());

        // Act
        client.cancelOrder(ORDER_ID);

        // Assert
        verify(requestSpec).body(Map.of("newStatus", OrderStatus.CANCELLED));
        verify(responseSpec).toBodilessEntity();
    }
}