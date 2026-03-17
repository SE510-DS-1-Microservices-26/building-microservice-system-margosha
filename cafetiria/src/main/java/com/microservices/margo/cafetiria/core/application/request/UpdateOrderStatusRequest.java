package com.microservices.margo.cafetiria.core.application.request;

import com.microservices.margo.cafetiria.core.domain.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(@NotNull OrderStatus newStatus) {}
