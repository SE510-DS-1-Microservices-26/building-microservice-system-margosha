package com.microservices.margo.user_service.core.domain.validation;

import com.microservices.margo.user_service.core.domain.validation.MinAge;
import com.microservices.margo.user_service.core.domain.validation.MinAgeValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.stream.Stream;

import static com.microservices.margo.user_service.core.domain.validation.ValidationConstants.MIN_AGE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MinAgeValidatorTest {

    private final MinAgeValidator validator = new MinAgeValidator();

    @BeforeEach
    void setUp() {
        MinAge annotation = mock(MinAge.class);
        when(annotation.value()).thenReturn(MIN_AGE);
        validator.initialize(annotation);
    }

    @ParameterizedTest(name = "isValid [{index}] {0}")
    @MethodSource("validDates")
    void isValid_validAge_returnsTrue(String reason, LocalDate birthDate) {
        // Act & Assert
        assertThat(validator.isValid(birthDate, null)).isTrue();
    }

    @ParameterizedTest(name = "isValid [{index}] {0}")
    @MethodSource("invalidDates")
    void isValid_invalidAge_returnsFalse(String reason, LocalDate birthDate) {
        // Act & Assert
        assertThat(validator.isValid(birthDate, null)).isFalse();
    }

    @Test
    void isValid_nullDate_returnsFalse() {
        // Act & Assert
        assertThat(validator.isValid(null, null)).isFalse();
    }

    static Stream<Arguments> validDates() {
        return Stream.of(
                arguments("exactly 14", LocalDate.now().minusYears(14)),
                arguments("18 years old", LocalDate.now().minusYears(18)),
                arguments("adult",  LocalDate.of(1990, 1, 1))
        );
    }

    static Stream<Arguments> invalidDates() {
        return Stream.of(
                arguments("13 years old", LocalDate.now().minusYears(13)),
                arguments("just born",LocalDate.now()),
                arguments("future date", LocalDate.now().plusDays(1))
        );
    }
}