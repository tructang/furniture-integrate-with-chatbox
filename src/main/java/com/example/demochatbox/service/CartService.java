package com.example.demochatbox.service;

import com.example.demochatbox.dto.CartDtos.CartItemResponse;
import com.example.demochatbox.dto.CartDtos.CartResponse;
import com.example.demochatbox.dto.CartDtos.CheckoutRequest;
import com.example.demochatbox.model.Cart;
import com.example.demochatbox.model.CartItem;
import com.example.demochatbox.model.CustomerOrder;
import com.example.demochatbox.model.OrderItem;
import com.example.demochatbox.model.OrderStatus;
import com.example.demochatbox.model.Product;
import com.example.demochatbox.repository.CartRepository;
import com.example.demochatbox.repository.OrderRepository;
import com.example.demochatbox.repository.ProductRepository;
import com.example.demochatbox.repository.UserAccountRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserAccountRepository userAccountRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public CartResponse getCart(Long userId) {
        return toResponse(getCartEntity(userId));
    }

    @Transactional
    public CartResponse addItem(Long userId, Long productId, Integer quantity) {
        Cart cart = getCartEntity(userId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay san pham"));
        Optional<CartItem> existing = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();
        if (existing.isPresent()) {
            existing.get().setQuantity(existing.get().getQuantity() + quantity);
        } else {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(quantity);
            cart.getItems().add(item);
        }
        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse updateItem(Long userId, Long itemId, Integer quantity) {
        Cart cart = getCartEntity(userId);
        CartItem item = cart.getItems().stream()
                .filter(cartItem -> cartItem.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay muc gio hang"));
        item.setQuantity(quantity);
        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse removeItem(Long userId, Long itemId) {
        Cart cart = getCartEntity(userId);
        cart.getItems().removeIf(item -> item.getId().equals(itemId));
        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public Long checkout(CheckoutRequest request) {
        Cart cart = getCartEntity(request.userId());
        if (cart.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Gio hang dang rong");
        }
        CustomerOrder order = new CustomerOrder();
        order.setUser(userAccountRepository.findById(request.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay nguoi dung")));
        order.setPaymentMethod(request.paymentMethod());
        order.setStatus(OrderStatus.PROCESSING);
        order.setShippingAddress(request.shippingAddress());
        order.setShippingProvider(request.shippingProvider() == null || request.shippingProvider().isBlank()
                ? "Noi bo"
                : request.shippingProvider());

        ArrayList<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cart.getItems()) {
            BigDecimal unitPrice = item.getProduct().getPromotionPrice() != null
                    ? item.getProduct().getPromotionPrice()
                    : item.getProduct().getPrice();
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(item.getProduct());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setUnitPrice(unitPrice);
            orderItems.add(orderItem);
            total = total.add(unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        order.setItems(orderItems);
        order.setTotalAmount(total);

        CustomerOrder savedOrder = orderRepository.save(order);
        cart.getItems().clear();
        cartRepository.save(cart);
        return savedOrder.getId();
    }

    private Cart getCartEntity(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setUser(userAccountRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay nguoi dung")));
            return cartRepository.save(cart);
        });
    }

    private CartResponse toResponse(Cart cart) {
        var items = cart.getItems().stream().map(item -> {
            BigDecimal unitPrice = item.getProduct().getPromotionPrice() != null
                    ? item.getProduct().getPromotionPrice()
                    : item.getProduct().getPrice();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            String image = item.getProduct().getImages().stream().findFirst()
                    .map(com.example.demochatbox.model.ProductImage::getImageUrl)
                    .orElse("");
            return new CartItemResponse(item.getId(), item.getProduct().getId(), item.getProduct().getName(),
                    image, item.getQuantity(), unitPrice, lineTotal);
        }).toList();
        BigDecimal total = items.stream().map(CartItemResponse::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartResponse(cart.getId(), items, total);
    }
}
