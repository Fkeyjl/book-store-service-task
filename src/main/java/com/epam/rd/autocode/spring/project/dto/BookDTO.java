package com.epam.rd.autocode.spring.project.dto;

import com.epam.rd.autocode.spring.project.model.Category;
import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import com.epam.rd.autocode.spring.project.validation.OnCreate;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookDTO{
    private Long id;
    
    @NotBlank(message = "Book name is required", groups = OnCreate.class)
    @Size(min = 1, max = 255, message = "Book name must be between 1 and 255 characters")
    private String name;

    @NotEmpty(message = "At least one category is required", groups = OnCreate.class)
    private Set<Category> categories;
    
    @NotBlank(message = "ISBN is required", groups = OnCreate.class)
    @Pattern(regexp = "^(?=(?:\\D*\\d){10}(?:(?:\\D*\\d){3})?$)[\\d-]+$", 
             message = "ISBN must be valid (10 or 13 digits)")
    private String isbn;
    
    @NotNull(message = "Age group is required", groups = OnCreate.class)
    private AgeGroup ageGroup;
    
    @NotNull(message = "Price is required", groups = OnCreate.class)
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Price must have maximum 10 integer digits and 2 decimal places")
    private BigDecimal price;

    @NotNull(message = "Publication date is required", groups = OnCreate.class)
    @PastOrPresent(message = "Publication date cannot be in the future")
    private LocalDate publicationDate;
    
    @NotBlank(message = "Author is required", groups = OnCreate.class)
    @Size(min = 1, max = 255, message = "Author name must be between 1 and 255 characters")
    private String author;

    @NotNull(message = "Number of pages required", groups = OnCreate.class)
    @Min(value = 1, message = "Number of pages must be at least 1")
    @Max(value = 10000, message = "Number of pages cannot exceed 10000")
    private Integer pages;

    @Size(max = 1000, message = "Characteristics cannot exceed 1000 characters")
    private String characteristics;
    
    @Size(max = 5000, message = "Description cannot exceed 5000 characters")
    private String description;
    
    @NotNull(message = "Language is required", groups = OnCreate.class)
    private Language language;
}
