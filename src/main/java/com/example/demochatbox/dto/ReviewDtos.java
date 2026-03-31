package com.example.demochatbox.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public final class ReviewDtos {

    private ReviewDtos() {
    }

    public record ReviewRequest(
            @NotNull Long userId,
            @NotNull @Min(1) @Max(5) Integer rating,
            @NotBlank String comment,
            String imageUrl
    ) {
    }

    public record ReviewResponse(String userName, Integer rating, String comment, String imageUrl, Instant createdAt) {
    }
}
