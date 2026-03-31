package com.example.demochatbox.controller;

import com.example.demochatbox.dto.AdminDtos.DashboardResponse;
import com.example.demochatbox.dto.AddressDtos.AddressRequest;
import com.example.demochatbox.dto.AddressDtos.AddressResponse;
import com.example.demochatbox.dto.AuthDtos.LoginRequest;
import com.example.demochatbox.dto.AuthDtos.RegisterRequest;
import com.example.demochatbox.dto.AuthDtos.UserResponse;
import com.example.demochatbox.dto.CartDtos.AddCartItemRequest;
import com.example.demochatbox.dto.CartDtos.CartResponse;
import com.example.demochatbox.dto.CartDtos.CheckoutRequest;
import com.example.demochatbox.dto.CartDtos.UpdateCartItemRequest;
import com.example.demochatbox.dto.ChatDtos.ChatRequest;
import com.example.demochatbox.dto.ChatDtos.ChatResponse;
import com.example.demochatbox.dto.CatalogDtos.CategoryResponse;
import com.example.demochatbox.dto.CatalogDtos.ProductCard;
import com.example.demochatbox.dto.CatalogDtos.ProductDetail;
import com.example.demochatbox.dto.OrderDtos.OrderResponse;
import com.example.demochatbox.dto.OrderDtos.UpdateStatusRequest;
import com.example.demochatbox.dto.ReviewDtos.ReviewRequest;
import com.example.demochatbox.dto.ReviewDtos.ReviewResponse;
import com.example.demochatbox.service.AdminService;
import com.example.demochatbox.service.AddressService;
import com.example.demochatbox.service.AuthService;
import com.example.demochatbox.service.CartService;
import com.example.demochatbox.service.ChatService;
import com.example.demochatbox.service.OrderService;
import com.example.demochatbox.service.ProductService;
import com.example.demochatbox.service.ReviewService;
import com.example.demochatbox.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ApiController {

    private final ProductService productService;
    private final AddressService addressService;
    private final AuthService authService;
    private final CartService cartService;
    private final OrderService orderService;
    private final ReviewService reviewService;
    private final ChatService chatService;
    private final AdminService adminService;
    private final SecurityUtils securityUtils;

    @GetMapping("/products")
    public List<ProductCard> products(@RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) String category,
                                      @RequestParam(required = false) String material,
                                      @RequestParam(required = false) String color,
                                      @RequestParam(required = false) BigDecimal minPrice,
                                      @RequestParam(required = false) BigDecimal maxPrice,
                                      @RequestParam(required = false) String sizeLabel) {
        return productService.search(keyword, category, material, color, minPrice, maxPrice, sizeLabel);
    }

    @GetMapping("/categories")
    public List<CategoryResponse> categories() {
        return productService.getCategories();
    }

    @GetMapping("/products/{slug}")
    public ProductDetail productDetail(@PathVariable String slug,
                                       @RequestParam(required = false) Long userId) {
        return productService.getBySlug(slug, userId);
    }

    @GetMapping("/products/recommendations/{userId}")
    public List<ProductCard> recommendations(@PathVariable Long userId) {
        return productService.recommendForUser(userId);
    }

    @PostMapping("/auth/register")
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/auth/login")
    public UserResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpServletRequest) {
        return authService.login(request, httpServletRequest);
    }

    @GetMapping("/auth/me")
    public UserResponse me() {
        Long userId = securityUtils.currentUserId();
        if (userId == null) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED);
        }
        return authService.getUser(userId);
    }

    @GetMapping("/users/{userId}")
    public UserResponse user(@PathVariable Long userId) {
        return authService.getUser(userId);
    }

    @GetMapping("/users/me")
    public UserResponse currentUser() {
        Long userId = securityUtils.currentUserId();
        if (userId == null) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED);
        }
        return authService.getUser(userId);
    }

    @GetMapping("/users/{userId}/addresses")
    public List<AddressResponse> userAddresses(@PathVariable Long userId) {
        return addressService.getUserAddresses(userId);
    }

    @GetMapping("/users/me/addresses")
    public List<AddressResponse> myAddresses() {
        Long userId = securityUtils.currentUserId();
        if (userId == null) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED);
        }
        return addressService.getUserAddresses(userId);
    }

    @PostMapping("/users/addresses")
    public AddressResponse addAddress(@Valid @RequestBody AddressRequest request) {
        return addressService.save(request);
    }

    @PostMapping("/users/me/addresses")
    public AddressResponse addMyAddress(@Valid @RequestBody AddressRequest request) {
        Long userId = securityUtils.currentUserId();
        if (userId == null) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED);
        }
        return addressService.save(userId, request);
    }

    @GetMapping("/cart/{userId}")
    public CartResponse cart(@PathVariable Long userId) {
        return cartService.getCart(userId);
    }

    @GetMapping("/cart/me")
    public CartResponse myCart() {
        Long userId = securityUtils.currentUserId();
        if (userId == null) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED);
        }
        return cartService.getCart(userId);
    }

    @PostMapping("/cart/{userId}/items")
    public CartResponse addCartItem(@PathVariable Long userId, @Valid @RequestBody AddCartItemRequest request) {
        return cartService.addItem(userId, request.productId(), request.quantity());
    }

    @PostMapping("/cart/me/items")
    public CartResponse addMyCartItem(@Valid @RequestBody AddCartItemRequest request) {
        Long userId = securityUtils.currentUserId();
        if (userId == null) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED);
        }
        return cartService.addItem(userId, request.productId(), request.quantity());
    }

    @PutMapping("/cart/{userId}/items/{itemId}")
    public CartResponse updateCartItem(@PathVariable Long userId, @PathVariable Long itemId,
                                       @Valid @RequestBody UpdateCartItemRequest request) {
        return cartService.updateItem(userId, itemId, request.quantity());
    }

    @PutMapping("/cart/me/items/{itemId}")
    public CartResponse updateMyCartItem(@PathVariable Long itemId, @Valid @RequestBody UpdateCartItemRequest request) {
        Long userId = securityUtils.currentUserId();
        if (userId == null) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED);
        }
        return cartService.updateItem(userId, itemId, request.quantity());
    }

    @DeleteMapping("/cart/{userId}/items/{itemId}")
    public CartResponse removeCartItem(@PathVariable Long userId, @PathVariable Long itemId) {
        return cartService.removeItem(userId, itemId);
    }

    @DeleteMapping("/cart/me/items/{itemId}")
    public CartResponse removeMyCartItem(@PathVariable Long itemId) {
        Long userId = securityUtils.currentUserId();
        if (userId == null) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED);
        }
        return cartService.removeItem(userId, itemId);
    }

    @PostMapping("/checkout")
    public Map<String, Long> checkout(@Valid @RequestBody CheckoutRequest request) {
        return Map.of("orderId", cartService.checkout(request));
    }

    @PostMapping("/checkout/me")
    public Map<String, Long> myCheckout(@Valid @RequestBody CheckoutRequest request) {
        Long userId = securityUtils.currentUserId();
        if (userId == null) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED);
        }
        CheckoutRequest checkoutRequest = new CheckoutRequest(userId, request.paymentMethod(), request.shippingAddress(), request.shippingProvider());
        return Map.of("orderId", cartService.checkout(checkoutRequest));
    }

    @GetMapping("/orders/{userId}")
    public List<OrderResponse> orders(@PathVariable Long userId) {
        return orderService.getUserOrders(userId);
    }

    @GetMapping("/orders/me")
    public List<OrderResponse> myOrders() {
        Long userId = securityUtils.currentUserId();
        if (userId == null) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED);
        }
        return orderService.getUserOrders(userId);
    }

    @PutMapping("/admin/orders/{orderId}/status")
    public OrderResponse updateOrderStatus(@PathVariable Long orderId, @Valid @RequestBody UpdateStatusRequest request) {
        return orderService.updateStatus(orderId, request);
    }

    @GetMapping("/products/{productId}/reviews")
    public List<ReviewResponse> reviews(@PathVariable Long productId) {
        return reviewService.getProductReviews(productId);
    }

    @PostMapping("/products/{productId}/reviews")
    public ReviewResponse createReview(@PathVariable Long productId, @Valid @RequestBody ReviewRequest request) {
        return reviewService.createReview(productId, request);
    }

    @PostMapping("/chat")
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        Long userId = securityUtils.currentUserId();
        if (userId == null) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED);
        }
        return chatService.ask(userId, request.message());
    }

    @GetMapping("/admin/dashboard")
    public DashboardResponse dashboard() {
        return adminService.getDashboard();
    }
}
