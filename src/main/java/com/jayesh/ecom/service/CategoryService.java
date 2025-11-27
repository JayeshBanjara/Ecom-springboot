package com.jayesh.ecom.service;

import com.jayesh.ecom.payload.CategoryDTO;
import com.jayesh.ecom.payload.CategoryResponse;



public interface CategoryService {

    CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    CategoryDTO getCategoryById(Long categoryId);

    CategoryDTO addCategory(CategoryDTO category);

    CategoryDTO deleteCategory(Long categoryId);

    CategoryDTO updateCategory(Long categoryId, CategoryDTO category);
}
