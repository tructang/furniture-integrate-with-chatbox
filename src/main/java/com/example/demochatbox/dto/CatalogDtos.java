package com.example.demochatbox.dto;

import com.example.demochatbox.model.ProductStatus;
import java.math.BigDecimal;
import java.util.List;

public final class CatalogDtos {

    private CatalogDtos() {
    }

    public record ProductCard(
            Long id,
            String slug,
            String name,
            String categoryCode,
            String categoryName,
            BigDecimal price,
            BigDecimal promotionPrice,
            String material,
            String color,
            String sizeLabel,
            ProductStatus status,
            String primaryImage
    ) {
    }

    public record ProductDetail(
            Long id,
            String slug,
            String name,
            String categoryCode,
            String categoryName,
            BigDecimal price,
            BigDecimal promotionPrice,
            String material,
            String color,
            String sizeLabel,
            Double widthCm,
            Double lengthCm,
            Double heightCm,
            String description,
            Integer stockQuantity,
            ProductStatus status,
            boolean arEnabled,
            String comboSuggestion,
            Double averageRating,
            List<String> images
    ) {
    }

    public record CategoryResponse(
            String code,
            String slug,
            String name,
            String description
    ) {
    }
}
