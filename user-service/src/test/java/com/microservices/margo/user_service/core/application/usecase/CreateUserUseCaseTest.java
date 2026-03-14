package com.microservices.margo.user_service.core.application.usecase;

import com.microservices.margo.user_service.core.application.exception.UserAlreadyExistsException;
import com.microservices.margo.user_service.core.application.mapper.UserMapper;
import com.microservices.margo.user_service.core.application.request.CreateUserRequest;
import com.microservices.margo.user_service.core.domain.User;
import com.microservices.margo.user_service.core.infrastructure.entity.UserEntity;
import com.microservices.margo.user_service.core.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateUserUseCaseTest {

    @Mock
    UserRepository userRepository;
    @Mock
    UserMapper userMapper;
    @InjectMocks
    CreateUserUseCase createUserUseCase;

    private static final CreateUserRequest REQUEST = new CreateUserRequest(
            "John", "Doe", "+380991234567", LocalDate.of(1990, 1, 1), "john@example.com");

    @Test
    void execute_validRequest_savesAndReturnsDomain() {
        // Arrange
        var entity = new UserEntity();
        var expected = new User(UUID.randomUUID(), "John", "Doe",
                "+380991234567", LocalDate.of(1990, 1, 1), "john@example.com", LocalDateTime.now());

        when(userMapper.toEntity(REQUEST)).thenReturn(entity);
        when(userRepository.save(entity)).thenReturn(entity);
        when(userMapper.toDomain(entity)).thenReturn(expected);

        // Act
        User result = createUserUseCase.execute(REQUEST);

        // Assert
        assertThat(result).isEqualTo(expected);
        verify(userRepository).save(entity);
    }

    @Test
    void execute_duplicateEmail_throwsUserAlreadyExistsException() {
        // Arrange
        when(userRepository.existsByEmail(REQUEST.email())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> createUserUseCase.execute(REQUEST))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("User with email john@example.com already exists!");

        verify(userRepository, never()).save(any());
    }

}