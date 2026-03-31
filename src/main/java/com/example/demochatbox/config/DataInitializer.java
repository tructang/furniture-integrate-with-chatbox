package com.example.demochatbox.config;

import com.example.demochatbox.model.Address;
import com.example.demochatbox.model.Cart;
import com.example.demochatbox.model.Category;
import com.example.demochatbox.model.CustomerOrder;
import com.example.demochatbox.model.OrderItem;
import com.example.demochatbox.model.OrderStatus;
import com.example.demochatbox.model.PaymentMethod;
import com.example.demochatbox.model.Product;
import com.example.demochatbox.model.ProductImage;
import com.example.demochatbox.model.ProductStatus;
import com.example.demochatbox.model.Review;
import com.example.demochatbox.model.UserAccount;
import com.example.demochatbox.repository.CartRepository;
import com.example.demochatbox.repository.OrderRepository;
import com.example.demochatbox.repository.ProductRepository;
import com.example.demochatbox.repository.ReviewRepository;
import com.example.demochatbox.repository.UserAccountRepository;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final UserAccountRepository userAccountRepository;
    private final CartRepository cartRepository;
    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Override
    public void run(String... args) {
        if (!seedEnabled || productRepository.count() > 0) {
            return;
        }

        Product sofa = createProduct("sofa-vang-bac-au", "Sofa vang Bac Au", Category.SOFA,
                bd("25900000"), bd("21900000"), "Vai cao cap", "Kem", "220x90cm",
                220.0, 90.0, 85.0, 15, true,
                "Sofa phong cach toi gian, de ket hop voi tham long ngan va den cay mau dong.",
                "Combo goi y: sofa + ban tra oval + tham len");
        addImages(sofa,
                "https://images.unsplash.com/photo-1505693416388-ac5ce068fe85",
                "https://images.unsplash.com/photo-1493663284031-b7e3aefcae8e");

        Product table = createProduct("ban-an-go-oc-cho", "Ban an go oc cho", Category.TABLE,
                bd("18900000"), bd("16900000"), "Go oc cho", "Nau dam", "180x90cm",
                180.0, 90.0, 75.0, 10, false,
                "Ban an 6 ghe, mat go day, phu hop can ho va nha pho hien dai.",
                "Combo goi y: ban an + 6 ghe boc nem");
        addImages(table,
                "https://images.unsplash.com/photo-1505409628601-edc9af17fda6",
                "https://images.unsplash.com/photo-1484154218962-a197022b5858");

        Product chair = createProduct("ghe-thu-gian-go", "Ghe thu gian go", Category.CHAIR,
                bd("6900000"), null, "Go soi", "Xam", "Don",
                78.0, 82.0, 74.0, 24, false,
                "Ghe don tua lung thap, hop goc doc sach hoac ban cong co mai che.",
                "Combo goi y: ghe don + ban phu tron");
        addImages(chair,
                "https://images.unsplash.com/photo-1519947486511-46149fa0a254");

        Product bed = createProduct("giuong-hop-go-sang", "Giuong hop go sang", Category.BED,
                bd("22900000"), bd("19900000"), "Go soi", "Nau sang", "1m8 x 2m",
                180.0, 200.0, 110.0, 8, false,
                "Giuong hop dau giuong boc vai, co ngan keo luu tru ben duoi.",
                "Combo goi y: giuong + tu 3 canh + tap dau giuong");
        addImages(bed,
                "https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?bed",
                "https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?bed2");

        Product cabinet = createProduct("tu-ao-can-lua", "Tu ao can lua", Category.CABINET,
                bd("27900000"), bd("24900000"), "Go cong nghiep chong am", "Trang sua", "240x60cm",
                240.0, 60.0, 220.0, 5, false,
                "Tu can lua tiet kiem dien tich, bo tri ngan treo va ngan keo theo nhu cau.",
                "Combo goi y: tu ao + giuong + ban trang diem");
        addImages(cabinet,
                "https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?cabinet");

        productRepository.saveAll(List.of(sofa, table, chair, bed, cabinet));

        UserAccount user = new UserAccount();
        user.setFullName("Demo Customer");
        user.setEmail("demo@noithat.vn");
        user.setPasswordHash(passwordEncoder.encode("123456"));
        user.setPhone("0909000999");
        user = userAccountRepository.save(user);

        Address address = new Address();
        address.setUser(user);
        address.setRecipientName("Demo Customer");
        address.setPhone("0909000999");
        address.setLine1("12 Nguyen Hue");
        address.setDistrict("Quan 1");
        address.setCity("TP.HCM");
        address.setDefaultAddress(true);
        user.getAddresses().add(address);
        userAccountRepository.save(user);

        Cart cart = new Cart();
        cart.setUser(user);
        cartRepository.save(cart);

        Review review = new Review();
        review.setProduct(sofa);
        review.setUser(user);
        review.setRating(5);
        review.setComment("Sofa ngoi em, mau ngoai doi dung nhu anh va giao hang dung hen.");
        review.setImageUrl("https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?review");
        reviewRepository.save(review);

        CustomerOrder order = new CustomerOrder();
        order.setUser(user);
        order.setPaymentMethod(PaymentMethod.COD);
        order.setStatus(OrderStatus.SHIPPING);
        order.setShippingProvider("Giao hang nhanh");
        order.setTrackingCode("GHN123456");
        order.setShippingAddress("12 Nguyen Hue, Quan 1, TP.HCM");
        order.setTotalAmount(bd("21900000"));
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(sofa);
        orderItem.setQuantity(1);
        orderItem.setUnitPrice(bd("21900000"));
        order.setItems(List.of(orderItem));
        orderRepository.save(order);
    }

    private Product createProduct(String slug, String name, Category category, BigDecimal price, BigDecimal promotionPrice,
                                  String material, String color, String sizeLabel, Double width, Double length,
                                  Double height, int stockQuantity, boolean arEnabled, String description,
                                  String comboSuggestion) {
        Product product = new Product();
        product.setSlug(slug);
        product.setName(name);
        product.setCategory(category);
        product.setPrice(price);
        product.setPromotionPrice(promotionPrice);
        product.setMaterial(material);
        product.setColor(color);
        product.setSizeLabel(sizeLabel);
        product.setWidthCm(width);
        product.setLengthCm(length);
        product.setHeightCm(height);
        product.setStockQuantity(stockQuantity);
        product.setStatus(stockQuantity > 0 ? ProductStatus.IN_STOCK : ProductStatus.OUT_OF_STOCK);
        product.setArEnabled(arEnabled);
        product.setDescription(description);
        product.setComboSuggestion(comboSuggestion);
        return product;
    }

    private void addImages(Product product, String... urls) {
        for (int i = 0; i < urls.length; i++) {
            ProductImage image = new ProductImage();
            image.setProduct(product);
            image.setImageUrl(urls[i]);
            image.setSortOrder(i);
            product.getImages().add(image);
        }
    }

    private BigDecimal bd(String value) {
        return new BigDecimal(value);
    }
}
