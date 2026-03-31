package com.example.demochatbox.service;

import com.example.demochatbox.dto.ChatDtos.ChatMessageResponse;
import com.example.demochatbox.dto.ChatDtos.ChatResponse;
import com.example.demochatbox.model.ChatMessage;
import com.example.demochatbox.model.MessageSender;
import com.example.demochatbox.model.Product;
import com.example.demochatbox.model.UserAccount;
import com.example.demochatbox.repository.ChatMessageRepository;
import com.example.demochatbox.repository.ProductRepository;
import com.example.demochatbox.repository.UserAccountRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserAccountRepository userAccountRepository;
    private final ProductRepository productRepository;

    @Value("${app.chat.ai-name:Noi That Assistant}")
    private String assistantName;

    @Transactional
    public ChatResponse ask(Long userId, String message) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay nguoi dung"));

        ChatMessage userMessage = new ChatMessage();
        userMessage.setUser(user);
        userMessage.setSender(MessageSender.USER);
        userMessage.setContent(message);
        chatMessageRepository.save(userMessage);

        String reply = generateReply(message);
        ChatMessage aiMessage = new ChatMessage();
        aiMessage.setUser(user);
        aiMessage.setSender(MessageSender.AI);
        aiMessage.setContent(reply);
        chatMessageRepository.save(aiMessage);

        return new ChatResponse(assistantName, reply, chatMessageRepository.findTop20ByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(item -> new ChatMessageResponse(item.getSender(), item.getContent(), item.getCreatedAt()))
                .collect(Collectors.collectingAndThen(Collectors.toList(), list -> {
                    java.util.Collections.reverse(list);
                    return list;
                })));
    }

    private String generateReply(String message) {
        String lower = message.toLowerCase();
        if (lower.contains("sofa")) {
            return "Neu phong khach nho, uu tien sofa mau sang, chat lieu vai de ve sinh va kich thuoc duoi 220cm.";
        }
        if (lower.contains("giuong")) {
            return "Voi phong ngu chuan, giuong go 1m6 x 2m ket hop tu dau giuong va den vang am la lua chon an toan.";
        }
        List<Product> matches = productRepository.findAll((root, query, criteriaBuilder) -> criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + lower + "%"),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + lower + "%")
        ));
        if (!matches.isEmpty()) {
            Product product = matches.get(0);
            return "San pham phu hop hien co la " + product.getName() + ", gia uu dai " +
                    (product.getPromotionPrice() != null ? product.getPromotionPrice() : product.getPrice()) + ".";
        }
        return "Toi co the tu van sofa, ban an, ghe, giuong, tu va combo noi that theo dien tich phong, mau sac va ngan sach cua ban.";
    }
}
