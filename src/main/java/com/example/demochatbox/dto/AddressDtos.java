package com.example.demochatbox.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class AddressDtos {

    private AddressDtos() {
    }

    public record AddressRequest(
            @NotNull Long userId,
            @NotBlank String recipientName,
            @NotBlank String phone,
            @NotBlank String line1,
            @NotBlank String district,
            @NotBlank String city,
            boolean defaultAddress
    ) {
    }

    public record AddressResponse(
            Long id,
            String recipientName,
            String phone,
            String line1,
            String district,
            String city,
            boolean defaultAddress
    ) {
    }
}
