package com.resourceshare.repository;

import com.resourceshare.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    List<Category> findByStatusOrderBySortOrderAsc(String status);
    Optional<Category> findByName(String name);
}
