package com.microservices.margo.workflow_service.api.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE;

@WebMvcTest(controllers = GlobalExceptionHandler.class)
@DisplayName("GlobalExceptionHandler tests")
class GlobalExceptionHandlerTest {

    @Autowired
    private GlobalExceptionHandler handler;

    @Test
    @DisplayName("returns 500 for unexpected Exception")
    void handleGeneralException_returns500() {
        // Arrange
        Exception exception = new RuntimeException("Something went wrong");

        // Act
        ResponseEntity<?> response = handler.handleGeneralException(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("Unexpected exception occurred");
    }

    @Test
    @DisplayName("returns 405 for HttpRequestMethodNotSupportedException")
    void handleHttpRequestMethodNotSupportedException_returns405() {
        // Arrange
        HttpRequestMethodNotSupportedException exception =
                new HttpRequestMethodNotSupportedException("PATCH");

        // Act
        ResponseEntity<?> response =
                handler.handleHttpRequestMethodNotSupportedException(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(METHOD_NOT_ALLOWED);
        assertThat(response.getBody()).isEqualTo(exception.getMessage());
    }

    @Test
    @DisplayName("returns 415 for HttpMediaTypeNotSupportedException")
    void handleHttpMediaTypeNotSupportedException_returns415() {
        // Arrange
        HttpMediaTypeNotSupportedException exception =
                new HttpMediaTypeNotSupportedException("application/xml");

        // Act
        ResponseEntity<?> response =
                handler.handleHttpMediaTypeNotSupportedException(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(UNSUPPORTED_MEDIA_TYPE);
        assertThat(response.getBody()).isEqualTo(exception.getMessage());
    }

    @Test
    @DisplayName("returns 400 with ErrorResponse for HttpMessageNotReadableException")
    void handleJsonParseError_returns400WithErrorResponse() {
        // Act & Assert
        ResponseEntity<?> response = handler.handleJsonParseError();

        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(response.getBody()).isInstanceOf(ErrorResponse.class);
        assertNotNull(response.getBody());
        assertThat(((ErrorResponse) response.getBody()).message())
                .isEqualTo("Invalid JSON format in request body");
    }

    @Test
    @DisplayName("returns 400 with joined field errors for MethodArgumentNotValidException")
    void handleValidationException_returns400WithFieldErrors() throws Exception {
        // Arrange
        MethodArgumentNotValidException exception = buildMethodArgumentNotValidException(
                "customerName", "Customer name must be specified.",
                "quantity", "Quantity must be at least 1."
        );

        // Act
        ResponseEntity<?> response = handler.handleValidationException(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertNotNull(response.getBody());
        String body = ((ErrorResponse) response.getBody()).message();
        assertThat(body).contains("customerName: Customer name must be specified.");
        assertThat(body).contains("quantity: Quantity must be at least 1.");
    }

    @Test
    @DisplayName("returns 400 with single field error message")
    void handleValidationException_singleViolation_returns400() throws Exception {
        // Arrange
        MethodArgumentNotValidException exception = buildMethodArgumentNotValidException(
                "itemName", "Item name must be specified."
        );

        // Act
        ResponseEntity<?> response = handler.handleValidationException(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertNotNull(response.getBody());
        assertThat(((ErrorResponse) response.getBody()).message())
                .isEqualTo("itemName: Item name must be specified.");
    }

    @Test
    @DisplayName("returns 400 with joined constraint messages for ConstraintViolationException")
    void handleConstraintViolationException_returns400() {
        // Arrange
        ConstraintViolationException exception = buildConstraintViolationException(
                "must not be blank", "must be greater than 0"
        );

        // Act
        ResponseEntity<?> response = handler.handleConstraintViolationException(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertNotNull(response.getBody());
        String body = ((ErrorResponse) response.getBody()).message();
        assertThat(body).contains("must not be blank");
        assertThat(body).contains("must be greater than 0");
    }

    @Test
    @DisplayName("returns 400 with message for IllegalArgumentException")
    void handleErrorResponses_illegalArgument_returns400() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("Bad argument");

        // Act
        ResponseEntity<?> response = handler.handleErrorResponses(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertNotNull(response.getBody());
        assertThat(((ErrorResponse) response.getBody()).message()).isEqualTo("Bad argument");
    }

    @Test
    @DisplayName("returns 400 with message for IllegalStateException")
    void handleErrorResponses_illegalState_returns400() {
        // Arrange
        IllegalStateException exception = new IllegalStateException("Illegal state");

        // Act
        ResponseEntity<?> response = handler.handleErrorResponses(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertNotNull(response.getBody());
        assertThat(((ErrorResponse) response.getBody()).message()).isEqualTo("Illegal state");
    }

    @Test
    @DisplayName("returns 404 with exception message when EntityNotFoundException has a message")
    void handleNotFoundException_withMessage_returns404() {
        // Arrange
        EntityNotFoundException exception = new EntityNotFoundException("Order not found");

        // Act
        ResponseEntity<?> response = handler.handleNotFoundException(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
        assertNotNull(response.getBody());
        assertThat(((ErrorResponse) response.getBody()).message()).isEqualTo("Order not found");
    }

    @Test
    @DisplayName("returns 404 with fallback message when EntityNotFoundException has null message")
    void handleNotFoundException_nullMessage_returnsFallback() {
        // Arrange
        EntityNotFoundException exception = new EntityNotFoundException((String) null);

        // Act
        ResponseEntity<?> response = handler.handleNotFoundException(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
        assertNotNull(response.getBody());
        assertThat(((ErrorResponse) response.getBody()).message()).isEqualTo("Not found!");
    }

    @Test
    @DisplayName("returns 400 with parameter name and value for MethodArgumentTypeMismatchException")
    void handleMethodArgumentTypeMismatch_returns400() {
        // Arrange
        MethodParameter methodParameter = mock(MethodParameter.class);
        MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException("not-a-uuid", String.class, "id", methodParameter, new RuntimeException());


        // Act
        ResponseEntity<?> response = handler.handleMethodArgumentTypeMismatch(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertNotNull(response.getBody());
        assertThat(response.getBody().toString())
                .contains("id")
                .contains("not-a-uuid");
    }

    @Test
    @DisplayName("returns 415 status code for HttpMediaTypeNotSupportedException")
    void handleHttpMediaTypeNotSupportedException() {
        // Arrange
        HttpMediaTypeNotSupportedException exception =
                new HttpMediaTypeNotSupportedException("application/xml");

        // Act
        ResponseEntity<?> response =
                handler.handleHttpMediaTypeNotSupportedException(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(UNSUPPORTED_MEDIA_TYPE);
        assertThat(response.getBody()).isEqualTo(exception.getMessage());
    }

    @Test
    @DisplayName("returns correct status and reason for ResponseStatusException")
    void handleResponseStatus_returnsCorrectStatusAndReason() {
        // Arrange
        ResponseStatusException exception = new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE, "Service is unavailable");

        // Act
        ResponseEntity<?> response = handler.handleResponseStatus(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertNotNull(response.getBody());
        assertThat(((ErrorResponse) response.getBody()).message())
                .isEqualTo("Service is unavailable");
    }

    @Test
    @DisplayName("returns 409 for DataIntegrityViolationException")
    void handleDataIntegrityViolation_returns409() {
        // Act
        ResponseEntity<?> response = handler.handleDataIntegrityViolation();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(CONFLICT);
        assertNotNull(response.getBody());
        assertThat(((ErrorResponse) response.getBody()).message())
                .isEqualTo("Failed to save entity because some rules where neglected.");
    }

    @Test
    @DisplayName("returns 404 for NoHandlerFoundException")
    void handleNoHandlerFound_returns404() {
        // Arrange
        NoHandlerFoundException exception = new NoHandlerFoundException(
                "GET", "/api/orders/unknown", new org.springframework.http.HttpHeaders());

        // Act
        ResponseEntity<?> response = handler.handleNoHandlerFound(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
        assertNotNull(response.getBody());
        assertThat(((ErrorResponse) response.getBody()).message())
                .contains("GET")
                .contains("/api/orders/unknown");
    }

    private MethodArgumentNotValidException buildMethodArgumentNotValidException(
            String... fieldAndMessagePairs) throws Exception {

        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "target");

        for (int i = 0; i < fieldAndMessagePairs.length; i += 2) {
            bindingResult.addError(new FieldError("target", fieldAndMessagePairs[i], fieldAndMessagePairs[i + 1]));
        }

        return new MethodArgumentNotValidException(
                new MethodParameter(Object.class.getDeclaredMethod("toString"), -1),
                bindingResult);
    }

    private ConstraintViolationException buildConstraintViolationException(String... messages) {
        Set<ConstraintViolation<?>> violations = Arrays.stream(messages)
                .map(message -> {
                    ConstraintViolation<?> cv = mock(ConstraintViolation.class);
                    when(cv.getMessage()).thenReturn(message);
                    return cv;
                })
                .collect(Collectors.toSet());
        return new ConstraintViolationException(violations);
    }
}