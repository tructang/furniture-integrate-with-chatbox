package com.example.demochatbox.repository;

import com.example.demochatbox.model.Category;
import com.example.demochatbox.model.Product;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySlug(String slug);

    List<Product> findTop6ByCategoryOrderByIdDesc(Category category);

    @Query("""
        select distinct p from Product p
        where (:keyword is null or lower(p.name) like lower(concat('%', :keyword, '%'))
            or lower(p.description) like lower(concat('%', :keyword, '%')))
          and (:category is null or p.category = :category)
          and (:material is null or lower(p.material) = lower(:material))
          and (:color is null or lower(p.color) = lower(:color))
          and (:minPrice is null or coalesce(p.promotionPrice, p.price) >= :minPrice)
          and (:maxPrice is null or coalesce(p.promotionPrice, p.price) <= :maxPrice)
          and (:sizeLabel is null or lower(p.sizeLabel) = lower(:sizeLabel))
        order by p.id desc
        """)
    List<Product> search(@Param("keyword") String keyword,
                         @Param("category") Category category,
                         @Param("material") String material,
                         @Param("color") String color,
                         @Param("minPrice") BigDecimal minPrice,
                         @Param("maxPrice") BigDecimal maxPrice,
                         @Param("sizeLabel") String sizeLabel);
}
