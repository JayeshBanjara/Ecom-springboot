package com.jayesh.ecom.service;

import com.jayesh.ecom.model.Category;

import java.util.List;

public interface CategoryService {

    List<Category> getAllCategories();

    Category getCategoryById(Long categoryId);

    void addCategory(Category category);

    void deleteCategory(Long categoryId);

    Category updateCategory(Long categoryId, Category category);
}
