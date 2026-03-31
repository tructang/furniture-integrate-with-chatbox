package com.example.demochatbox.service;

import com.example.demochatbox.dto.OrderDtos.OrderItemResponse;
import com.example.demochatbox.dto.OrderDtos.OrderResponse;
import com.example.demochatbox.dto.OrderDtos.UpdateStatusRequest;
import com.example.demochatbox.model.CustomerOrder;
import com.example.demochatbox.repository.OrderRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public List<OrderResponse> getUserOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public OrderResponse updateStatus(Long orderId, UpdateStatusRequest request) {
        CustomerOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay don hang"));
        order.setStatus(request.status());
        order.setTrackingCode(request.trackingCode());
        order.setShippingProvider(request.shippingProvider() == null ? order.getShippingProvider() : request.shippingProvider());
        return toResponse(orderRepository.save(order));
    }

    private OrderResponse toResponse(CustomerOrder order) {
        return new OrderResponse(order.getId(), order.getPaymentMethod(), order.getStatus(), order.getTotalAmount(),
                order.getShippingProvider(), order.getTrackingCode(), order.getShippingAddress(), order.getCreatedAt(),
                order.getItems().stream()
                        .map(item -> new OrderItemResponse(item.getProduct().getName(), item.getQuantity(), item.getUnitPrice()))
                        .toList());
    }
}
