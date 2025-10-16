package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.CategoryDTO;
import com.epam.rd.autocode.spring.project.service.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/categories")
    public String getCategories(Model model) {
        List<CategoryDTO> categories = categoryService.getCategories();
        model.addAttribute("categories", categories);
        return "user/categories";
    }
}
