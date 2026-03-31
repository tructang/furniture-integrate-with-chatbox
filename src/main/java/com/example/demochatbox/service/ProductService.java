package com.example.demochatbox.service;

import com.example.demochatbox.dto.CatalogDtos.ProductCard;
import com.example.demochatbox.dto.CatalogDtos.ProductDetail;
import com.example.demochatbox.model.Category;
import com.example.demochatbox.model.Product;
import com.example.demochatbox.repository.ProductRepository;
import com.example.demochatbox.repository.ReviewRepository;
import com.example.demochatbox.repository.UserAccountRepository;
import com.example.demochatbox.repository.ViewHistoryRepository;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final ViewHistoryRepository viewHistoryRepository;
    private final UserAccountRepository userAccountRepository;

    @Transactional(readOnly = true)
    public List<ProductCard> search(String keyword, Category category, String material, String color,
                                    BigDecimal minPrice, BigDecimal maxPrice, String sizeLabel) {
        return productRepository.search(keyword, category, material, color, minPrice, maxPrice, sizeLabel)
                .stream()
                .map(this::toCard)
                .toList();
    }

    @Transactional
    public ProductDetail getBySlug(String slug, Long userId) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay san pham"));
        if (userId != null) {
            recordView(userId, product);
        }
        return toDetail(product);
    }

    @Transactional(readOnly = true)
    public List<ProductCard> recommendForUser(Long userId) {
        LinkedHashSet<Category> recentCategories = viewHistoryRepository.findRecentByUserId(userId).stream()
                .map(history -> history.getProduct().getCategory())
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        return recentCategories.stream()
                .flatMap(category -> productRepository.findTop6ByCategoryOrderByIdDesc(category).stream())
                .sorted(Comparator.comparing(Product::getId).reversed())
                .limit(6)
                .map(this::toCard)
                .toList();
    }

    private void recordView(Long userId, Product product) {
        com.example.demochatbox.model.ViewHistory history = new com.example.demochatbox.model.ViewHistory();
        history.setUser(userAccountRepository.getReferenceById(userId));
        history.setProduct(product);
        viewHistoryRepository.save(history);
    }

    private ProductCard toCard(Product product) {
        String image = product.getImages().stream()
                .sorted(Comparator.comparing(ProductImage -> ProductImage.getSortOrder() == null ? 0 : ProductImage.getSortOrder()))
                .map(com.example.demochatbox.model.ProductImage::getImageUrl)
                .findFirst()
                .orElse("https://images.unsplash.com/photo-1505693416388-ac5ce068fe85");
        return new ProductCard(product.getId(), product.getSlug(), product.getName(), product.getCategory(),
                product.getPrice(), product.getPromotionPrice(), product.getMaterial(), product.getColor(),
                product.getSizeLabel(), product.getStatus(), image);
    }

    private ProductDetail toDetail(Product product) {
        return new ProductDetail(
                product.getId(),
                product.getSlug(),
                product.getName(),
                product.getCategory(),
                product.getPrice(),
                product.getPromotionPrice(),
                product.getMaterial(),
                product.getColor(),
                product.getSizeLabel(),
                product.getWidthCm(),
                product.getLengthCm(),
                product.getHeightCm(),
                product.getDescription(),
                product.getStockQuantity(),
                product.getStatus(),
                product.isArEnabled(),
                product.getComboSuggestion(),
                reviewRepository.averageRating(product.getId()),
                product.getImages().stream()
                        .sorted(Comparator.comparing(image -> image.getSortOrder() == null ? 0 : image.getSortOrder()))
                        .map(com.example.demochatbox.model.ProductImage::getImageUrl)
                        .toList()
        );
    }
}
