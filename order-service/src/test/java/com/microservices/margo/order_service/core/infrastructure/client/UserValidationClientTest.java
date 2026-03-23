package com.microservices.margo.order_service.core.infrastructure.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.net.SocketTimeoutException;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@DisplayName("UserValidationClient tests")
@MockitoSettings(strictness = Strictness.LENIENT)
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
    @DisplayName("does not throw when user is found")
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
    @DisplayName("rethrows ResourceAccessException when service is unreachable")
    void validateUserExists_shouldRethrowResourceAccessException_whenServiceUnreachable() {
        // Arrange
        UUID userId = UUID.randomUUID();
        doReturn(responseSpec).when(responseSpec).onStatus(any(), any());
        doThrow(new ResourceAccessException("Connection refused"))
                .when(responseSpec).toBodilessEntity();

        // Act & Assert
        assertThatThrownBy(() -> userValidationClient.validateUserExists(userId))
                .isInstanceOf(ResourceAccessException.class);
    }

    @ParameterizedTest(name = "fallback returns {1} for {0}")
    @MethodSource("fallbackExceptions")
    @DisplayName("fallback returns correct status based on exception cause")
    void fallback_returnsCorrectStatus(ResourceAccessException exception, HttpStatus expectedStatus) {
        // Arrange
        UUID userId = UUID.randomUUID();

        // Act & Assert
        assertThatThrownBy(() -> userValidationClient.fallback(exception, userId))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(expectedStatus);
    }

    static Stream<Arguments> fallbackExceptions() {
        return Stream.of(
                Arguments.of(
                        new ResourceAccessException("timeout", new SocketTimeoutException()),
                        HttpStatus.GATEWAY_TIMEOUT
                ),
                Arguments.of(
                        new ResourceAccessException("connection refused"),
                        HttpStatus.SERVICE_UNAVAILABLE
                )
        );
    }
}