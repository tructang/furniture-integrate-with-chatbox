package com.example.demochatbox.controller;

import com.example.demochatbox.model.Category;
import com.example.demochatbox.service.ProductService;
import com.example.demochatbox.service.ReviewService;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final ProductService productService;
    private final ReviewService reviewService;

    @GetMapping("/")
    public String home(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Category category,
                       @RequestParam(required = false) String material,
                       @RequestParam(required = false) String color,
                       @RequestParam(required = false) BigDecimal minPrice,
                       @RequestParam(required = false) BigDecimal maxPrice,
                       @RequestParam(required = false) String sizeLabel,
                       Model model) {
        model.addAttribute("products", productService.search(keyword, category, material, color, minPrice, maxPrice, sizeLabel));
        model.addAttribute("categories", Category.values());
        model.addAttribute("selectedCategory", category);
        return "index";
    }

    @GetMapping("/products/{slug}")
    public String detail(@PathVariable String slug, Model model) {
        var product = productService.getBySlug(slug, 1L);
        model.addAttribute("product", product);
        model.addAttribute("reviews", reviewService.getProductReviews(product.id()));
        return "product-detail";
    }
}
