package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.model.Category;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Set;

public interface CategoryService {
    List<Category> getCategories();
    Page<Category> getCategoriesPage(int page, int size);
    Set<Category> resolveCategoriesForUpdate(Set<Category> categoriesFromDto);
}
