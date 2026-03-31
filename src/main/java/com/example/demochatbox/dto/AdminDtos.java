package com.example.demochatbox.dto;

import com.example.demochatbox.model.OrderStatus;
import java.math.BigDecimal;
import java.util.Map;

public final class AdminDtos {

    private AdminDtos() {
    }

    public record DashboardResponse(
            long totalUsers,
            long totalProducts,
            long totalOrders,
            BigDecimal revenue,
            Map<OrderStatus, Long> orderStatusBreakdown
    ) {
    }
}
