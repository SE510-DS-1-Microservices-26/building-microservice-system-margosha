package com.microservices.margo.cafetiria.core.application.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

import static com.microservices.margo.cafetiria.core.domain.validation.ValidationConstants.MAX_NAME_LENGTH;

public record CreateOrderRequest(
        @NotBlank(message = "Customer name must be specified.")
        @Size(max = MAX_NAME_LENGTH, message = "Customer name must consist at most of 255 symbols")
        String customerName,

        @NotBlank(message = "Item name must be specified.")
        @Size(max = MAX_NAME_LENGTH, message = "Item name must consist at most of 255 symbols")
        String itemName,

        @Positive(message = "Quantity must be at least 1.")
        int quantity,

        @NotNull(message = "Price name must be specified.")
        @PositiveOrZero(message = "Price cannot be negative.")
        BigDecimal price)
{}