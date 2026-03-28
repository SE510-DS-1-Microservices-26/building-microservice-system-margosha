package com.microservices.margo.workflow_service.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.health.actuate.endpoint.HealthDescriptor;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.health.actuate.endpoint.IndicatedHealthDescriptor;
import org.springframework.boot.health.contributor.Status;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static org.mockito.Mockito.mock;
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
    private HealthDescriptor realDescriptor;

    @BeforeEach
    void setUp() {
       realDescriptor = mock(IndicatedHealthDescriptor.class);
       when(healthEndpoint.health()).thenReturn(realDescriptor);
    }

    @Test
    void when_healthEndpointStatusIsUp_shouldReturnOk() throws Exception {
        // Arrange
        when(realDescriptor.getStatus()).thenReturn(Status.UP);

        // Act & Assert
        mockMvc.perform(get(HEALTH_PATH))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @MethodSource("provideErrorStatuses")
    void when_healthEndpointStatusIsDown_shouldReturnErrorStatusCode(Status status) throws Exception {
        // Arrange
        when(realDescriptor.getStatus()).thenReturn(status);

        // Act & Assert
        mockMvc.perform(get(HEALTH_PATH))
                .andExpect(status().isServiceUnavailable());
    }

    private static Stream<Status> provideErrorStatuses() {
        return Stream.of(Status.UNKNOWN, Status.DOWN, Status.OUT_OF_SERVICE);
    }
}