package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.model.Category;
import com.epam.rd.autocode.spring.project.repo.CategoryRepository;
import com.epam.rd.autocode.spring.project.service.CategoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<Category> getCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Page<Category> getCategoriesPage(int page, int size) {
        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        Pageable pageable = PageRequest.of(page, size, sort);
        return categoryRepository.findAll(pageable);
    }

    @Override
    public Set<Category> resolveCategoriesForUpdate(Set<Category> categoriesFromDto) {
        if (categoriesFromDto == null || categoriesFromDto.contains(null) || categoriesFromDto.isEmpty()) {
            return Collections.emptySet();
        }
        Set<Long> categoryIds = categoriesFromDto.stream()
                .map(Category::getId)
                .filter(id -> id != null && categoryRepository.existsById(id))
                .collect(Collectors.toSet());
        return new HashSet<>(categoryRepository.findAllById(categoryIds));
    }
}
