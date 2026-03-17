package com.microservices.margo.gateway.filter;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.UUID;

import static com.microservices.margo.gateway.filter.CorrelationIdFilter.CORRELATION_ID_HEADER;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@DisplayName("CorrelationIdFilter tests")
@ExtendWith(MockitoExtension.class)
class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    @DisplayName("uses existing correlation id from request header")
    void doFilter_usesExistingCorrelationId() throws Exception {
        // Arrange
        String existingId = UUID.randomUUID().toString();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader(CORRELATION_ID_HEADER, existingId);
        FilterChain chain = mock(FilterChain.class);

        // Act
        filter.doFilter(request, response, chain);

        // Assert
        assertThat(response.getHeader(CORRELATION_ID_HEADER)).isEqualTo(existingId);
        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("generates correlation id when header is missing")
    void doFilter_generatesCorrelationId_whenHeaderMissing() throws Exception {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        // Act
        filter.doFilter(request, response, chain);

        // Assert
        assertThat(response.getHeader(CORRELATION_ID_HEADER)).isNotBlank();
        verify(chain).doFilter(request, response);
    }

    @ParameterizedTest
    @EmptySource
    @DisplayName("generates correlation id when header is blank")
    void doFilter_generatesCorrelationId_whenHeaderIsBlank(String blank) throws Exception {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader(CORRELATION_ID_HEADER, blank);
        FilterChain chain = mock(FilterChain.class);

        // Act
        filter.doFilter(request, response, chain);

        // Assert
        assertThat(response.getHeader(CORRELATION_ID_HEADER)).isNotBlank();
        verify(chain).doFilter(request, response);
    }
}