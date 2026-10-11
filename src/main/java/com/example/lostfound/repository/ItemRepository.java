package com.example.lostfound.repository;

import com.example.lostfound.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByType(String type);

    List<Item> findByCategory(String category);

    List<Item> findByStatus(String status);

    // Find matching items without case-sensitive text comparisons
    List<Item> findByTypeIgnoreCaseAndCategoryIgnoreCaseAndStatusIgnoreCase(
            String type,
            String category,
            String status
    );
}