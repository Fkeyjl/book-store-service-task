package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import com.epam.rd.autocode.spring.project.service.BookService;
import com.epam.rd.autocode.spring.project.service.CategoryService;
import com.epam.rd.autocode.spring.project.validation.OnCreate;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/books")
public class BookController {
    private final BookService bookService;
    private final CategoryService categoryService;

    public BookController(BookService bookService, CategoryService categoryService) {
        this.bookService = bookService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String showBooks(
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String ageGroup,
            @RequestParam(required = false) String minPrice,
            @RequestParam(required = false) String maxPrice,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort,
            Model model) {

        Language languageEnum = parseLanguage(language);
        AgeGroup ageGroupEnum = parseAgeGroup(ageGroup);
        BigDecimal minPriceBD = parseBigDecimal(minPrice);
        BigDecimal maxPriceBD = parseBigDecimal(maxPrice);
        Sort sortObj = parseSort(sort);
        
        List<BookDTO> books = bookService.getFilteredAndSortedBooks(
                languageEnum, ageGroupEnum, minPriceBD, maxPriceBD, search, sortObj);
        
        model.addAttribute("books", books);
        return "user/books";
    }
    
    private Language parseLanguage(String language) {
        try {
            return language != null && !language.isEmpty() ? Language.valueOf(language) : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    private AgeGroup parseAgeGroup(String ageGroup) {
        try {
            return ageGroup != null && !ageGroup.isEmpty() ? AgeGroup.valueOf(ageGroup) : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    private BigDecimal parseBigDecimal(String value) {
        try {
            return value != null && !value.isEmpty() ? new BigDecimal(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    private Sort parseSort(String sort) {
        if (sort == null || sort.isEmpty()) {
            return null;
        }
        return switch (sort) {
            case "name" -> Sort.by("name").ascending();
            case "author" -> Sort.by("author").ascending();
            case "price_asc" -> Sort.by("price").ascending();
            case "price_desc" -> Sort.by("price").descending();
            case "date_desc" -> Sort.by("publicationDate").descending();
            default -> null;
        };
    }

    @GetMapping("/{id}")
    public String showBookView(@PathVariable("id") Long id, Model model) {
        BookDTO book = bookService.getBookById(id);
        model.addAttribute("book", book);
        return "user/book-view";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public String showBookEditForm(@PathVariable("id") Long id, Model model) {
        BookDTO book = bookService.getBookById(id);
        model.addAttribute("bookId", id);
        model.addAttribute("book", book);
        model.addAttribute("allAgeGroups", AgeGroup.values());
        model.addAttribute("allLanguages", Language.values());
        model.addAttribute("allCategories", categoryService.getCategories());
        return "management/book-details";
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public String editBook(@PathVariable("id") Long id,
                           @ModelAttribute("book") BookDTO bookDTO) {
        bookService.updateBook(id, bookDTO);
        return "redirect:/books/" + id;
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public String deleteBook(@PathVariable("id") Long id) {
        bookService.deleteBook(id);
        return "redirect:/books";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public String showCreateBookForm(Model model) {
        model.addAttribute("bookDTO", new BookDTO());
        model.addAttribute("allAgeGroups", AgeGroup.values());
        model.addAttribute("allLanguages", Language.values());
        model.addAttribute("allCategories", categoryService.getCategories());

        return "user/create-book";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public String createBook(@Validated(OnCreate.class) @ModelAttribute("bookDTO") BookDTO bookDTO) {
        bookService.addBook(bookDTO);
        return "redirect:/books";
    }
}
