package com.microservices.margo.user_service.core.application.usecase;

import com.microservices.margo.user_service.core.application.mapper.UserMapper;
import com.microservices.margo.user_service.core.domain.User;
import com.microservices.margo.user_service.core.infrastructure.entity.UserEntity;
import com.microservices.margo.user_service.core.infrastructure.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUserUseCaseTest {

    @Mock
    UserRepository userRepository;
    @Mock
    UserMapper userMapper;
    @InjectMocks
    GetUserUseCase getUserUseCase;

    private static final UUID ID = UUID.randomUUID();

    @Test
    void execute_existingId_returnsDomainUser() {
        // Arrange
        var entity = new UserEntity();
        var expected = new User(ID, "John", "Doe", "+380991234567",
                LocalDate.of(1990, 1, 1), "john@example.com", LocalDateTime.now());

        when(userRepository.findById(ID)).thenReturn(Optional.of(entity));
        when(userMapper.toDomain(entity)).thenReturn(expected);

        // Act
        User result = getUserUseCase.execute(ID);

        // Assert
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void execute_nonExistingId_throwsEntityNotFoundException() {
        // Arrange
        when(userRepository.findById(ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> getUserUseCase.execute(ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(ID.toString());
    }
}