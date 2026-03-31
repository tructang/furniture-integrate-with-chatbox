package com.example.demochatbox.service;

import com.example.demochatbox.dto.ReviewDtos.ReviewRequest;
import com.example.demochatbox.dto.ReviewDtos.ReviewResponse;
import com.example.demochatbox.model.Review;
import com.example.demochatbox.repository.ProductRepository;
import com.example.demochatbox.repository.ReviewRepository;
import com.example.demochatbox.repository.UserAccountRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserAccountRepository userAccountRepository;

    @Transactional(readOnly = true)
    public List<ReviewResponse> getProductReviews(Long productId) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId).stream()
                .map(review -> new ReviewResponse(review.getUser().getFullName(), review.getRating(), review.getComment(),
                        review.getImageUrl(), review.getCreatedAt()))
                .toList();
    }

    @Transactional
    public ReviewResponse createReview(Long productId, ReviewRequest request) {
        Review review = new Review();
        review.setProduct(productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay san pham")));
        review.setUser(userAccountRepository.findById(request.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay nguoi dung")));
        review.setRating(request.rating());
        review.setComment(request.comment());
        review.setImageUrl(request.imageUrl());
        Review saved = reviewRepository.save(review);
        return new ReviewResponse(saved.getUser().getFullName(), saved.getRating(), saved.getComment(),
                saved.getImageUrl(), saved.getCreatedAt());
    }
}
