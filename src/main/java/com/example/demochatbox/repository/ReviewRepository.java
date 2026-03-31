package com.example.demochatbox.repository;

import com.example.demochatbox.model.Review;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProductIdOrderByCreatedAtDesc(Long productId);

    @Query("select coalesce(avg(r.rating), 0) from Review r where r.product.id = :productId")
    Double averageRating(@Param("productId") Long productId);
}
