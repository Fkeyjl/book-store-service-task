package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.CategoryDTO;
import com.epam.rd.autocode.spring.project.model.Category;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Set;

public interface CategoryService {
    List<CategoryDTO> getCategories();
    Page<CategoryDTO> getCategoriesPage(int page, int size);
    CategoryDTO getCategoryById(Long id);
    Set<Category> resolveCategoriesForInsert(Set<CategoryDTO> categoriesFromDto);
}
