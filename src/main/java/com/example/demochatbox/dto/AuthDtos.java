package com.example.demochatbox.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank String fullName,
            @Email String email,
            @NotBlank String password,
            String phone
    ) {
    }

    public record LoginRequest(
            @Email String email,
            @NotBlank String password
    ) {
    }

    public record UserResponse(Long id, String fullName, String email, String phone) {
    }
}
