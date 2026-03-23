package com.microservices.margo.order_service.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("HealthController")
@WebMvcTest(HealthController.class)
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HealthEndpoint healthEndpoint;

    private static final String HEALTH_PATH = "/health";

    @Test
    @DisplayName("returns 200 when health status is UP")
    void when_healthEndpointStatusIsUp_shouldReturnOk() throws Exception {
        // Arrange
        when(healthEndpoint.health()).thenReturn(Health.up().build());

        // Act & Assert
        mockMvc.perform(get(HEALTH_PATH))
                .andExpect(status().isOk());
    }

    @ParameterizedTest(name = "returns 503 when status is {0}")
    @MethodSource("provideErrorStatuses")
    @DisplayName("returns 503 when health status is not UP")
    void when_healthEndpointStatusIsDown_shouldReturnErrorStatusCode(Health health) throws Exception {
        // Arrange
        when(healthEndpoint.health()).thenReturn(health);

        // Act & Assert
        mockMvc.perform(get(HEALTH_PATH))
                .andExpect(status().isServiceUnavailable());
    }

    private static Stream<Health> provideErrorStatuses() {
        return Stream.of(
                Health.unknown().build(),
                Health.down().build(),
                Health.status(Status.OUT_OF_SERVICE).build()
        );
    }
}