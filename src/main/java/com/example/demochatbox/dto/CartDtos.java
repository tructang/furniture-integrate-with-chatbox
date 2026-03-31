package com.example.demochatbox.dto;

import com.example.demochatbox.model.PaymentMethod;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public final class CartDtos {

    private CartDtos() {
    }

    public record AddCartItemRequest(
            @NotNull Long productId,
            @NotNull @Min(1) Integer quantity
    ) {
    }

    public record UpdateCartItemRequest(@NotNull @Min(1) Integer quantity) {
    }

    public record CartItemResponse(
            Long itemId,
            Long productId,
            String productName,
            String imageUrl,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal
    ) {
    }

    public record CartResponse(
            Long cartId,
            List<CartItemResponse> items,
            BigDecimal totalAmount
    ) {
    }

    public record CheckoutRequest(
            @NotNull Long userId,
            @NotNull PaymentMethod paymentMethod,
            @NotBlank String shippingAddress,
            String shippingProvider
    ) {
    }
}
