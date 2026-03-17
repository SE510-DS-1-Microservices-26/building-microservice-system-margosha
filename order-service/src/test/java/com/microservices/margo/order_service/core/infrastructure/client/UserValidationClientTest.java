package com.microservices.margo.order_service.core.infrastructure.client;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@DisplayName("UserValidationClient tests")
@ExtendWith(MockitoExtension.class)
class UserValidationClientTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private UserValidationClient userValidationClient;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userValidationClient, "usersUrl",
                "http://localhost:8080/api/users/");
        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(anyString());
        doReturn(responseSpec).when(requestHeadersSpec).retrieve();
    }

    @Test
    void validateUserExists_shouldNotThrow_whenUserFound() {
        // Arrange
        UUID userId = UUID.randomUUID();

        doReturn(responseSpec).when(responseSpec).onStatus(any(), any());
        doReturn(null).when(responseSpec).toBodilessEntity();

        // Act & Assert
        assertThatCode(() -> userValidationClient.validateUserExists(userId))
                .doesNotThrowAnyException();
    }

    @Test
    void validateUserExists_shouldThrow400_whenUserNotFound() {
        // Arrange
        UUID userId = UUID.randomUUID();

        doThrow(new IllegalArgumentException("User not found: " + userId))
                .when(responseSpec).onStatus(any(), any());

        // Act & Assert
        assertThatThrownBy(() -> userValidationClient.validateUserExists(userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void validateUserExists_shouldThrow503_whenServiceUnreachable() {
        // Arrange
        UUID userId = UUID.randomUUID();

        doReturn(responseSpec).when(responseSpec).onStatus(any(), any());
        doThrow(new ResourceAccessException("Connection refused"))
                .when(responseSpec).toBodilessEntity();

        // Act & Assert
        assertThatThrownBy(() -> userValidationClient.validateUserExists(userId))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }
}