package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.CategoryDTO;
import com.epam.rd.autocode.spring.project.model.Category;
import com.epam.rd.autocode.spring.project.repo.CategoryRepository;
import com.epam.rd.autocode.spring.project.service.CategoryService;
import org.modelmapper.ModelMapper;
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
    private final ModelMapper modelMapper;

    public CategoryServiceImpl(CategoryRepository categoryRepository, ModelMapper modelMapper) {
        this.categoryRepository = categoryRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<CategoryDTO> getCategories() {
        return categoryRepository.findAll().stream()
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .toList();
    }

    @Override
    public Page<CategoryDTO> getCategoriesPage(int page, int size) {
        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Category> categoryPage = categoryRepository.findAll(pageable);
        return categoryPage.map(category -> modelMapper.map(category, CategoryDTO.class));
    }

    @Override
    public Set<Category> resolveCategoriesForUpdate(Set<CategoryDTO> categoriesFromDto) {
        if (categoriesFromDto == null || categoriesFromDto.contains(null) || categoriesFromDto.isEmpty()) {
            return Collections.emptySet();
        }
        Set<Long> categoryIds = categoriesFromDto.stream()
                .map(CategoryDTO::getId)
                .filter(id -> id != null && categoryRepository.existsById(id))
                .collect(Collectors.toSet());
        return new HashSet<>(categoryRepository.findAllById(categoryIds));
    }
}
