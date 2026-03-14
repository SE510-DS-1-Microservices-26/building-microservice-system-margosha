package com.microservices.margo.order_service.core.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OrderStatus tests")
public class OrderStatusTest {

    @ParameterizedTest(name = "{0} → {1} = {2}")
    @MethodSource("transitions")
    @DisplayName("matches the full transition matrix")
    void canTransitionToWorksProperly(OrderStatus from, OrderStatus to, boolean expected) {
        assertThat(from.canTransitionTo(to)).isEqualTo(expected);
    }

    public static Stream<Arguments> transitions() {
        return Stream.of(
                Arguments.of(OrderStatus.PENDING, OrderStatus.PENDING,   false),
                Arguments.of(OrderStatus.PENDING, OrderStatus.CONFIRMED,  true),
                Arguments.of(OrderStatus.PENDING, OrderStatus.DELIVERED,  false),
                Arguments.of(OrderStatus.PENDING, OrderStatus.CANCELLED,  true),
                Arguments.of(OrderStatus.CONFIRMED, OrderStatus.PENDING,   false),
                Arguments.of(OrderStatus.CONFIRMED, OrderStatus.CONFIRMED,  false),
                Arguments.of(OrderStatus.CONFIRMED, OrderStatus.DELIVERED,  true),
                Arguments.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED,  true),
                Arguments.of(OrderStatus.DELIVERED, OrderStatus.PENDING,   false),
                Arguments.of(OrderStatus.DELIVERED, OrderStatus.CONFIRMED,  false),
                Arguments.of(OrderStatus.DELIVERED, OrderStatus.DELIVERED,  false),
                Arguments.of(OrderStatus.DELIVERED, OrderStatus.CANCELLED,  false),
                Arguments.of(OrderStatus.CANCELLED, OrderStatus.PENDING,   false),
                Arguments.of(OrderStatus.CANCELLED, OrderStatus.CONFIRMED,  false),
                Arguments.of(OrderStatus.CANCELLED, OrderStatus.DELIVERED,  false),
                Arguments.of(OrderStatus.CANCELLED, OrderStatus.CANCELLED,  false)
        );
    }
}
