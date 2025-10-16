package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import com.epam.rd.autocode.spring.project.service.BookService;
import com.epam.rd.autocode.spring.project.service.CategoryService;
import com.epam.rd.autocode.spring.project.validation.OnCreate;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

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
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Language language,
            @RequestParam(required = false) AgeGroup ageGroup,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort,
            Pageable pageable,
            Model model) {

        Pageable pageableWithSort = createPageableWithSort(pageable, sort);

        Page<BookDTO> booksPage = bookService.getFilteredAndSortedPage(
                categoryId, language, ageGroup, minPrice, maxPrice, search, pageableWithSort);

        model.addAttribute("books", booksPage);
        model.addAttribute("categories", categoryService.getCategories());
        return "user/books";
    }

    private Pageable createPageableWithSort(Pageable pageable, String sortParam) {
        if (sortParam == null || sortParam.isEmpty()) {
            return pageable;
        }

        Sort sort = switch (sortParam) {
            case "name" -> Sort.by("name").ascending();
            case "author" -> Sort.by("author").ascending();
            case "price_asc" -> Sort.by("price").ascending();
            case "price_desc" -> Sort.by("price").descending();
            case "date_desc" -> Sort.by("publicationDate").descending();
            default -> Sort.unsorted();
        };

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
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
                           @Valid @ModelAttribute("book") BookDTO bookDTO) {
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
        if (!model.containsAttribute("creatingError")) {
            model.addAttribute("creatingError", null);
        }
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
