package com.example.demochatbox.repository;

import com.example.demochatbox.model.CustomerOrder;
import com.example.demochatbox.model.OrderStatus;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {

    @EntityGraph(attributePaths = {"items", "items.product"})
    List<CustomerOrder> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByStatus(OrderStatus status);
}
