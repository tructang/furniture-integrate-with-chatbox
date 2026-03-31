package com.example.demochatbox.repository;

import com.example.demochatbox.model.ChatMessage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findTop20ByUserIdOrderByCreatedAtDesc(Long userId);
}
