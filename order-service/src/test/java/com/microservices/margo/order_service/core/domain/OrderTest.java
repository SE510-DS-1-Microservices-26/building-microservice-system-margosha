package com.microservices.margo.order_service.core.domain;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.microservices.margo.order_service.TestData.pendingOrder;
import static org.assertj.core.api.Assertions.*;

@DisplayName("Order domain tests")
class OrderTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    private Order orderWithStatus(OrderStatus status) {
        return pendingOrder().toBuilder().status(status).build();
    }

    private Set<String> violationMessages(Order order) {
        return validator.validate(order).stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());
    }

    @Test
    @DisplayName("defaults status to PENDING when null is provided")
    void defaultsStatusToPending_whenStatusIsNull() {
        // Arrange & Act
        Order order = Order.builder()
                .id(UUID.randomUUID())
                .ownerUserId(UUID.randomUUID())
                .itemName("Latte")
                .quantity(1)
                .price(BigDecimal.ONE)
                .status(null)
                .build();

        // Assert
        assertThat(order.status()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    @DisplayName("preserves explicitly provided status")
    void preservesExplicitStatus() {
        // Arrange & Act
        Order order = orderWithStatus(OrderStatus.CONFIRMED);

        // Assert
        assertThat(order.status()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("returns a new Order instance because original is immutable")
    void returnsNewInstance() {
        // Arrange
        Order original = pendingOrder();

        // Act
        Order updated = original.changeStatus(OrderStatus.CONFIRMED);

        // Assert
        assertThat(updated).isNotSameAs(original);
        assertThat(original.status()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    @DisplayName("PENDING -> CONFIRMED succeeds")
    void pendingToConfirmed() {
        // Arrange
        Order order = pendingOrder();

        // Act
        Order updated = order.changeStatus(OrderStatus.CONFIRMED);

        // Assert
        assertThat(updated.status()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("PENDING -> CANCELLED succeeds")
    void pendingToCancelled() {
        // Arrange
        Order order = pendingOrder();

        // Act
        Order updated = order.changeStatus(OrderStatus.CANCELLED);

        // Assert
        assertThat(updated.status()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("CONFIRMED -> DELIVERED succeeds")
    void confirmedToDelivered() {
        // Arrange
        Order order = orderWithStatus(OrderStatus.CONFIRMED);

        // Act
        Order updated = order.changeStatus(OrderStatus.DELIVERED);

        // Assert
        assertThat(updated.status()).isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    @DisplayName("CONFIRMED -> CANCELLED succeeds")
    void confirmedToCancelled() {
        //Arrange
        Order order = orderWithStatus(OrderStatus.CONFIRMED);

        // Act
        Order updated = order.changeStatus(OrderStatus.CANCELLED);

        // Assert
        assertThat(updated.status()).isEqualTo(OrderStatus.CANCELLED);
    }

    @ParameterizedTest(name = "{0} -> {1} throws IllegalStateException")
    @MethodSource("invalidTransitions")
    @DisplayName("throws IllegalStateException for illegal transitions")
    void throwsOnIllegalTransition(OrderStatus from, OrderStatus to) {
        // Arrange
        Order order = orderWithStatus(from);

        // Act & Assert
        assertThatThrownBy(() -> order.changeStatus(to))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(from.name())
                .hasMessageContaining(to.name());
    }

    static Stream<Arguments> invalidTransitions() {
        return Stream.of(
                Arguments.of(OrderStatus.PENDING,    OrderStatus.DELIVERED),
                Arguments.of(OrderStatus.PENDING,    OrderStatus.PENDING),
                Arguments.of(OrderStatus.CONFIRMED,  OrderStatus.CONFIRMED),
                Arguments.of(OrderStatus.CONFIRMED,  OrderStatus.PENDING),
                Arguments.of(OrderStatus.DELIVERED,  OrderStatus.CONFIRMED),
                Arguments.of(OrderStatus.DELIVERED,  OrderStatus.CANCELLED),
                Arguments.of(OrderStatus.DELIVERED,  OrderStatus.PENDING),
                Arguments.of(OrderStatus.DELIVERED,  OrderStatus.DELIVERED),
                Arguments.of(OrderStatus.CANCELLED,  OrderStatus.PENDING),
                Arguments.of(OrderStatus.CANCELLED,  OrderStatus.CONFIRMED),
                Arguments.of(OrderStatus.CANCELLED,  OrderStatus.DELIVERED),
                Arguments.of(OrderStatus.CANCELLED,  OrderStatus.CANCELLED)
        );
    }

    @Test
    @DisplayName("preserves all other fields after status change")
    void preservesOtherFields() {
        // Arrange
        Order original = pendingOrder();

        // Act
        Order updated = original.changeStatus(OrderStatus.CONFIRMED);

        // Assert
        assertThat(updated.id()).isEqualTo(original.id());
        assertThat(updated.ownerUserId()).isEqualTo(original.ownerUserId());
        assertThat(updated.itemName()).isEqualTo(original.itemName());
        assertThat(updated.quantity()).isEqualTo(original.quantity());
        assertThat(updated.price()).isEqualByComparingTo(original.price());
        assertThat(updated.createdAt()).isEqualTo(original.createdAt());
    }

    @Test
    @DisplayName("allows valid order")
    void order_isValid() {
        // Arrange
        Order order = pendingOrder();

        // Act
        Set<ConstraintViolation<Order>> violations = validator.validate(order);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("does not allow null ownerUserId")
    void customerId_isNull() {
        // Arrange
        Order order = pendingOrder().toBuilder().ownerUserId(null).build();

        // Act
        Set<String> messages = violationMessages(order);

        // Assert
        assertThat(messages).contains("Customer id is required.");
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @DisplayName("does not allow blank/null itemName")
    void itemName_isInvalid(String name) {
        // Arrange
        Order order = pendingOrder().toBuilder().itemName(name).build();

        // Act
        Set<String> messages = violationMessages(order);

        // Assert
        assertThat(messages).contains("Item name must be specified.");
    }

    @Test
    @DisplayName("does not allow itemName longer than 255 characters")
    void itemName_isTooLong() {
        // Arrange
        Order order = pendingOrder().toBuilder()
                .itemName("A".repeat(256))
                .build();

        // Act
        Set<String> messages = violationMessages(order);

        // Assert
        assertThat(messages).contains("Item name must consist at most of 255 symbols");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 4, 89})
    @DisplayName("allows valid quantity which is bigger than 0")
    void quantity_isValid(int quantity) {
        // Arrange
        Order order = pendingOrder().toBuilder().quantity(quantity).build();

        // Act
        Set<ConstraintViolation<Order>> violations = validator.validate(order);

        // Assert
        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    @DisplayName("fails validation when quantity is less than 1")
    void quantity_isInvalid(int invalidQuantity) {
        // Arrange
        Order order = pendingOrder().toBuilder()
                .quantity(invalidQuantity)
                .build();

        // Act
        Set<String> messages = violationMessages(order);

        // Assert
        assertThat(messages).contains("Quantity must be at least 1.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.0", "0.01", "99.99", "1000000"})
    @DisplayName("allows valid price")
    void price_isValid(String priceValue) {
        // Arrange
        BigDecimal validPrice = new BigDecimal(priceValue);
        Order order = pendingOrder().toBuilder()
                .price(validPrice)
                .build();

        // Act
        Set<ConstraintViolation<Order>> violations = validator.validate(order);

        // Assert
        assertThat(violations).isEmpty();
    }


    @ParameterizedTest
    @CsvSource(value = {"-0.01", "-100.00", "-88999"})
    @DisplayName("does not allow negative price")
    void price_isInvalid(String priceInput) {
        // Arrange
        BigDecimal price = new BigDecimal(priceInput);

        Order order = pendingOrder().toBuilder()
                .price(price)
                .build();

        // Act
        Set<String> messages = violationMessages(order);

        // Assert
        assertThat(messages).contains( "Price cannot be negative.");
    }

    @Test
    @DisplayName("does not allow null price")
    void price_isInvalid() {
        // Arrange
        Order order = pendingOrder().toBuilder()
                .price(null)
                .build();

        // Act
        Set<String> messages = violationMessages(order);

        // Assert
        assertThat(messages).contains("Price name must be specified.");
    }

    @Test
    @DisplayName("reports all violations at once when multiple fields are invalid")
    void multipleViolations_shouldBeReportedTogether() {
        // Arrange
        Order order = pendingOrder().toBuilder()
                .ownerUserId(null)
                .itemName("")
                .quantity(0)
                .price(new BigDecimal("-1"))
                .build();

        // Act
        Set<String> messages = violationMessages(order);

        // Assert
        assertThat(messages).containsExactlyInAnyOrder(
                "Customer id is required.",
                "Item name must be specified.",
                "Quantity must be at least 1.",
                "Price cannot be negative."
        );
    }
}