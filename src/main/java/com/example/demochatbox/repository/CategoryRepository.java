package com.example.demochatbox.repository;

import com.example.demochatbox.model.Category;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, String> {

    List<Category> findAllByOrderBySortOrderAscCodeAsc();
}
