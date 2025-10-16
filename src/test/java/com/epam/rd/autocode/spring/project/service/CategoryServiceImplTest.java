package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.CategoryDTO;
import com.epam.rd.autocode.spring.project.model.Category;
import com.epam.rd.autocode.spring.project.repo.CategoryRepository;
import com.epam.rd.autocode.spring.project.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category1;
    private Category category2;
    private CategoryDTO categoryDTO1;
    private CategoryDTO categoryDTO2;

    @BeforeEach
    void setUp() {
        category1 = new Category();
        category1.setId(1L);
        category1.setName("Fiction");

        category2 = new Category();
        category2.setId(2L);
        category2.setName("Science");

        categoryDTO1 = new CategoryDTO();
        categoryDTO1.setId(1L);
        categoryDTO1.setName("Fiction");

        categoryDTO2 = new CategoryDTO();
        categoryDTO2.setId(2L);
        categoryDTO2.setName("Science");
    }

    @Test
    void testGetCategories_ShouldReturnListOfCategoryDTOs() {
        List<Category> categories = Arrays.asList(category1, category2);
        when(categoryRepository.findAll()).thenReturn(categories);
        when(modelMapper.map(category1, CategoryDTO.class)).thenReturn(categoryDTO1);
        when(modelMapper.map(category2, CategoryDTO.class)).thenReturn(categoryDTO2);

        List<CategoryDTO> result = categoryService.getCategories();

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(categoryDTO1, categoryDTO2);
        verify(categoryRepository, times(1)).findAll();
        verify(modelMapper, times(2)).map(any(Category.class), eq(CategoryDTO.class));
    }

    @Test
    void testGetCategoriesPage_ShouldReturnPageOfCategoryDTOs() {
        Page<Category> categoryPage = new PageImpl<>(Arrays.asList(category1, category2));
        when(categoryRepository.findAll(any(PageRequest.class))).thenReturn(categoryPage);
        when(modelMapper.map(category1, CategoryDTO.class)).thenReturn(categoryDTO1);
        when(modelMapper.map(category2, CategoryDTO.class)).thenReturn(categoryDTO2);

        Page<CategoryDTO> result = categoryService.getCategoriesPage(0, 10);

        assertThat(result).hasSize(2);
        assertThat(result.getContent()).containsExactly(categoryDTO1, categoryDTO2);
        verify(categoryRepository, times(1)).findAll(any(PageRequest.class));
    }

    @Test
    void testResolveCategoriesForUpdate_WithValidCategories_ShouldReturnSetOfCategories() {
        Set<CategoryDTO> categoryDTOs = new HashSet<>(Arrays.asList(categoryDTO1, categoryDTO2));
        Set<Long> categoryIds = Set.of(1L, 2L);

        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(categoryRepository.existsById(2L)).thenReturn(true);
        when(categoryRepository.findAllById(categoryIds)).thenReturn(Arrays.asList(category1, category2));

        Set<Category> result = categoryService.resolveCategoriesForUpdate(categoryDTOs);

        assertThat(result).hasSize(2);
        assertThat(result).contains(category1, category2);
        verify(categoryRepository, times(2)).existsById(anyLong());
        verify(categoryRepository, times(1)).findAllById(anySet());
    }

    @Test
    void testResolveCategoriesForUpdate_WithNullCategories_ShouldReturnEmptySet() {
        Set<Category> result = categoryService.resolveCategoriesForUpdate(null);

        assertThat(result).isEmpty();
        verify(categoryRepository, never()).existsById(anyLong());
        verify(categoryRepository, never()).findAllById(anySet());
    }

    @Test
    void testResolveCategoriesForUpdate_WithEmptyCategories_ShouldReturnEmptySet() {
        Set<CategoryDTO> emptySet = new HashSet<>();

        Set<Category> result = categoryService.resolveCategoriesForUpdate(emptySet);

        assertThat(result).isEmpty();
        verify(categoryRepository, never()).existsById(anyLong());
        verify(categoryRepository, never()).findAllById(anySet());
    }

    @Test
    void testResolveCategoriesForUpdate_WithNonExistentCategories_ShouldFilterThem() {
        Set<CategoryDTO> categoryDTOs = new HashSet<>(Arrays.asList(categoryDTO1, categoryDTO2));

        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(categoryRepository.existsById(2L)).thenReturn(false);
        when(categoryRepository.findAllById(Set.of(1L))).thenReturn(Collections.singletonList(category1));

        Set<Category> result = categoryService.resolveCategoriesForUpdate(categoryDTOs);

        assertThat(result).hasSize(1);
        assertThat(result).contains(category1);
        verify(categoryRepository, times(2)).existsById(anyLong());
        verify(categoryRepository, times(1)).findAllById(anySet());
    }
}
