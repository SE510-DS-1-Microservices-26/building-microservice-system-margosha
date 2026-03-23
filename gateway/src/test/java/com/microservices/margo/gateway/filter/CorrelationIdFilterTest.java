package com.microservices.margo.gateway.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@DisplayName("CorrelationIdFilter tests")
@ExtendWith(MockitoExtension.class)
class CorrelationIdFilterTest {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(filter, "correlationIdHeader", CORRELATION_ID_HEADER);
        ReflectionTestUtils.setField(filter, "mdcKey", "correlationId");
        MDC.clear();
    }

    @Test
    @DisplayName("uses existing correlation id from request header")
    void doFilter_usesExistingCorrelationId() throws Exception {
        // Arrange
        String existingId = UUID.randomUUID().toString();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CORRELATION_ID_HEADER, existingId);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        ArgumentCaptor<ServletRequest> requestCaptor = ArgumentCaptor.forClass(ServletRequest.class);

        // Act
        filter.doFilter(request, response, chain);

        // Assert
        assertThat(response.getHeader(CORRELATION_ID_HEADER)).isEqualTo(existingId);
        verify(chain).doFilter(requestCaptor.capture(), org.mockito.ArgumentMatchers.any());
        assertThat(((HttpServletRequestWrapper) requestCaptor.getValue()).getHeader(CORRELATION_ID_HEADER))
                .isEqualTo(existingId);
    }

    @Test
    @DisplayName("generates correlation id when header is missing")
    void doFilter_generatesCorrelationId_whenHeaderMissing() throws Exception {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        ArgumentCaptor<ServletRequest> requestCaptor = ArgumentCaptor.forClass(ServletRequest.class);

        // Act
        filter.doFilter(request, response, chain);

        // Assert
        String generatedId = response.getHeader(CORRELATION_ID_HEADER);
        assertThat(generatedId).isNotBlank().matches("[0-9a-f-]{36}");
        verify(chain).doFilter(requestCaptor.capture(), org.mockito.ArgumentMatchers.any());
        assertThat(((HttpServletRequestWrapper) requestCaptor.getValue()).getHeader(CORRELATION_ID_HEADER))
                .isEqualTo(generatedId);
    }

    @ParameterizedTest
    @EmptySource
    @DisplayName("generates correlation id when header is blank")
    void doFilter_generatesCorrelationId_whenHeaderIsBlank(String blank) throws Exception {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CORRELATION_ID_HEADER, blank);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        ArgumentCaptor<ServletRequest> requestCaptor = ArgumentCaptor.forClass(ServletRequest.class);

        // Act
        filter.doFilter(request, response, chain);

        // Assert
        String generatedId = response.getHeader(CORRELATION_ID_HEADER);
        assertThat(generatedId).isNotBlank().matches("[0-9a-f-]{36}");
        verify(chain).doFilter(requestCaptor.capture(), org.mockito.ArgumentMatchers.any());
        assertThat(((HttpServletRequestWrapper) requestCaptor.getValue()).getHeader(CORRELATION_ID_HEADER))
                .isEqualTo(generatedId);
    }

    @Test
    @DisplayName("clears MDC after request completes")
    void doFilter_clearsMdcAfterRequest() throws Exception {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        // Act
        filter.doFilter(request, response, chain);

        // Assert
        assertThat(MDC.get("correlationId")).isNull();
    }

    @Test
    @DisplayName("wrapper returns generated id via getHeaders enumeration")
    void wrapper_returnsGeneratedId_viaGetHeaders() throws Exception {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        ArgumentCaptor<ServletRequest> requestCaptor = ArgumentCaptor.forClass(ServletRequest.class);

        // Act
        filter.doFilter(request, response, chain);

        // Assert
        verify(chain).doFilter(requestCaptor.capture(), org.mockito.ArgumentMatchers.any());
        HttpServletRequestWrapper wrapper = (HttpServletRequestWrapper) requestCaptor.getValue();
        String idFromEnumeration = wrapper.getHeaders(CORRELATION_ID_HEADER).nextElement();
        assertThat(idFromEnumeration).isEqualTo(response.getHeader(CORRELATION_ID_HEADER));
    }
}