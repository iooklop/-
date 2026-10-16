package com.resourceshare.service;

import com.resourceshare.entity.Category;
import com.resourceshare.exception.CustomException;
import com.resourceshare.repository.CategoryRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> listActive() {
        return categoryRepository.findByStatusOrderBySortOrderAsc("ACTIVE");
    }

    public List<Category> listAll() {
        return categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "sortOrder"));
    }

    public Category create(Category category) {
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new CustomException("分类名称不能为空");
        }
        if (categoryRepository.findByName(category.getName()).isPresent()) {
            throw new CustomException("分类已存在");
        }
        if (category.getStatus() == null) {
            category.setStatus("ACTIVE");
        }
        if (category.getParentId() == null) {
            category.setParentId(0);
        }
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        return categoryRepository.save(category);
    }

    public Category update(Integer id, Category category) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new CustomException("分类不存在"));
        if (category.getName() != null) {
            existing.setName(category.getName());
        }
        if (category.getIcon() != null) {
            existing.setIcon(category.getIcon());
        }
        if (category.getSortOrder() != null) {
            existing.setSortOrder(category.getSortOrder());
        }
        if (category.getStatus() != null) {
            existing.setStatus(category.getStatus());
        }
        return categoryRepository.save(existing);
    }

    public void delete(Integer id) {
        categoryRepository.deleteById(id);
    }
}
