package com.example.demochatbox.dto;

import com.example.demochatbox.model.MessageSender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

public final class ChatDtos {

    private ChatDtos() {
    }

    public record ChatRequest(@NotNull Long userId, @NotBlank String message) {
    }

    public record ChatMessageResponse(MessageSender sender, String content, Instant createdAt) {
    }

    public record ChatResponse(String assistantName, String reply, List<ChatMessageResponse> history) {
    }
}
