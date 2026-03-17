package com.microservices.margo.cafetiria.core.domain;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static com.microservices.margo.cafetiria.core.domain.validation.ValidationConstants.MAX_NAME_LENGTH;

@Builder(toBuilder = true)
public record Order(
        UUID id,

        @NotBlank(message = "Customer name must be specified.")
        @Size(max = MAX_NAME_LENGTH, message = "Customer name must consist at most of 255 symbols")
        String customerName,

        @NotBlank(message = "Item name must be specified.")
        @Size(max = MAX_NAME_LENGTH, message = "Item name must consist at most of 255 symbols")
        String itemName,

        @Min(value = 1, message = "Quantity must be at least 1.")
        int quantity,

        @NotNull(message = "Price name must be specified.")
        @PositiveOrZero(message = "Price cannot be negative.")
        BigDecimal price,

        OrderStatus status,
        LocalDateTime createdAt
) {
    public Order {
        if (status == null) {
            status = OrderStatus.PENDING;
        }
    }
    public Order changeStatus(OrderStatus newStatus) {
        if (!status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    String.format("Cannot change order status from %s to %s", status, newStatus));
        }
        return this.toBuilder().status(newStatus).build();
    }
}