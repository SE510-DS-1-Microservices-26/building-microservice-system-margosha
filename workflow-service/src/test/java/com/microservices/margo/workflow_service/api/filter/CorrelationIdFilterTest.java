package com.microservices.margo.workflow_service.api.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static com.microservices.margo.workflow_service.TestData.CORRELATION_ID_HEADER;
import static com.microservices.margo.workflow_service.TestData.MDC_KEY;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CorrelationIdFilter tests")
class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(filter, "correlationIdHeader", CORRELATION_ID_HEADER);
        ReflectionTestUtils.setField(filter, "mdcKey", MDC_KEY);
    }

    @Test
    @DisplayName("uses existing X-Correlation-Id from request header")
    void usesExistingCorrelationId() throws Exception {
        // Arrange
        String correlationId = UUID.randomUUID().toString();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CORRELATION_ID_HEADER, correlationId);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        // Act
        filter.doFilterInternal(request, response, chain);

        // Assert
        assertThat(MDC.get(MDC_KEY)).isNull();
        assertThat(response.getHeader(CORRELATION_ID_HEADER)).isNull();
    }

    @ParameterizedTest(name = "generates new UUID when header is [{0}]")
    @NullAndEmptySource
    @DisplayName("generates new UUID when X-Correlation-Id is missing or blank")
    void generatesNewUuid_whenHeaderMissingOrBlank(String headerValue) throws Exception {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        if (headerValue != null) {
            request.addHeader(CORRELATION_ID_HEADER, headerValue);
        }
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        // Act
        filter.doFilterInternal(request, response, chain);

        // Assert
        assertThat(response.getHeader(CORRELATION_ID_HEADER)).isNull();
    }

    @Test
    @DisplayName("clears MDC after request completes")
    void clearsMdcAfterRequest() throws Exception {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        // Act
        filter.doFilterInternal(request, response, chain);

        // Assert
        assertThat(MDC.get(MDC_KEY)).isNull();
    }
}