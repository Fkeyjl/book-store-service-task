package com.epam.rd.autocode.spring.project.utils;

import com.epam.rd.autocode.spring.project.dto.CategoryDTO;
import com.epam.rd.autocode.spring.project.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StringToCategoryDTOConverter implements Converter<String, CategoryDTO> {

    private final CategoryService categoryService;

    @Override
    public CategoryDTO convert(String id) {
        try {
            Long categoryId = Long.parseLong(id);
            return categoryService.getCategoryById(categoryId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid category ID: " + id, e);
        }
    }
}
