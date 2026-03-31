package com.example.demochatbox.dto;

import com.example.demochatbox.model.OrderStatus;
import com.example.demochatbox.model.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class OrderDtos {

    private OrderDtos() {
    }

    public record OrderItemResponse(String productName, Integer quantity, BigDecimal unitPrice) {
    }

    public record OrderResponse(
            Long id,
            PaymentMethod paymentMethod,
            OrderStatus status,
            BigDecimal totalAmount,
            String shippingProvider,
            String trackingCode,
            String shippingAddress,
            Instant createdAt,
            List<OrderItemResponse> items
    ) {
    }

    public record UpdateStatusRequest(@NotNull OrderStatus status, String trackingCode, String shippingProvider) {
    }
}
