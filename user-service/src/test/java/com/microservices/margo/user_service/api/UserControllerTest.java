package com.microservices.margo.user_service.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.microservices.margo.user_service.core.application.request.CreateUserRequest;
import com.microservices.margo.user_service.core.application.usecase.CreateUserUseCase;
import com.microservices.margo.user_service.core.application.usecase.GetUserUseCase;
import com.microservices.margo.user_service.core.domain.User;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @MockitoBean
    private CreateUserUseCase createUserUseCase;

    @MockitoBean
    private GetUserUseCase getUserUseCase;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final LocalDateTime NOW = LocalDateTime.now();

    private static User user() {
        return new User(USER_ID, "John", "Doe", "+380991234567",
                LocalDate.of(1990, 1, 1), "john@example.com", NOW);
    }

    private static CreateUserRequest createUserRequest() {
        return new CreateUserRequest("John", "Doe", "+380991234567",
                LocalDate.of(1990, 1, 1), "john@example.com");
    }

    @Test
    void create_validRequest_returnsCreated() throws Exception {
        // Arrange
        when(createUserUseCase.execute(any())).thenReturn(user());

        // Act & Assert
        mockMvc.perform(post("/users")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserRequest())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/users/" + USER_ID)))
                .andExpect(jsonPath("$.id").value(USER_ID.toString()))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @ParameterizedTest(name = "create_invalidRequest [{index}] {0}")
    @MethodSource("invalidRequests")
    void create_invalidRequest_returnsBadRequest(String reason, CreateUserRequest request) throws Exception {
        // Act & Assert
        mockMvc.perform(post("/users")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    static Stream<Arguments> invalidRequests() {
        return Stream.of(
                arguments("blank name",
                        new CreateUserRequest("", "Doe", "+380991234567", LocalDate.of(1990, 1, 1), "john@example.com")),
                arguments("blank surname",
                        new CreateUserRequest("John", "", "+380991234567", LocalDate.of(1990, 1, 1), "john@example.com")),
                arguments("invalid phone",
                        new CreateUserRequest("John", "Doe", "abc", LocalDate.of(1990, 1, 1), "john@example.com")),
                arguments("invalid email",
                        new CreateUserRequest("John", "Doe", "+380991234567", LocalDate.of(1990, 1, 1), "not-an-email")),
                arguments("blank email",
                        new CreateUserRequest("John", "Doe", "+380991234567", LocalDate.of(1990, 1, 1), "")),
                arguments("underage user",
                        new CreateUserRequest("John", "Doe", "+380991234567", LocalDate.now().minusYears(13), "john@example.com")),
                arguments("future birth date",
                        new CreateUserRequest("John", "Doe", "+380991234567", LocalDate.now().plusDays(1), "john@example.com")),
                arguments("name too long",
                        new CreateUserRequest("A".repeat(256), "Doe", "+380991234567", LocalDate.of(1990, 1, 1), "john@example.com")),
                arguments("surname too long",
                        new CreateUserRequest("John", "A".repeat(256), "+380991234567", LocalDate.of(1990, 1, 1), "john@example.com")),
                arguments("email too long",
                        new CreateUserRequest("John", "Doe", "+380991234567", LocalDate.of(1990, 1, 1), "a".repeat(92) + "@test.com"))
        );
    }

    @Test
    void getById_existingUser_returnsOk() throws Exception {
        // Arrange
        when(getUserUseCase.execute(USER_ID)).thenReturn(user());

        // Act & Assert
        mockMvc.perform(get("/users/{id}", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_ID.toString()))
                .andExpect(jsonPath("$.name").value("John"));
    }

    @Test
    void getById_nonExistingUser_returnsNotFound() throws Exception {
        // Arrange
        when(getUserUseCase.execute(any())).thenThrow(new EntityNotFoundException("User not found"));

        // Act & Assert
        mockMvc.perform(get("/users/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}
