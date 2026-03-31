package com.example.demochatbox.service;

import com.example.demochatbox.dto.AdminDtos.DashboardResponse;
import com.example.demochatbox.model.OrderStatus;
import com.example.demochatbox.repository.OrderRepository;
import com.example.demochatbox.repository.ProductRepository;
import com.example.demochatbox.repository.UserAccountRepository;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserAccountRepository userAccountRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        BigDecimal revenue = orderRepository.findAll().stream()
                .map(order -> order.getTotalAmount() == null ? BigDecimal.ZERO : order.getTotalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<OrderStatus, Long> breakdown = Arrays.stream(OrderStatus.values())
                .collect(Collectors.toMap(status -> status, orderRepository::countByStatus));
        return new DashboardResponse(userAccountRepository.count(), productRepository.count(), orderRepository.count(), revenue, breakdown);
    }
}
