package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.BookSummaryDTO;
import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;

public interface BookService {
    List<BookDTO> getAllBooks();
    
    List<BookDTO> getFilteredAndSortedBooks(
            Language language,
            AgeGroup ageGroup,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String searchTerm,
            Sort sort
    );

    Page<BookSummaryDTO> getNewestBooksPaged(int page, int size);

    BookDTO getBookByName(String name);

    BookDTO getBookById(Long id);
    
    List<BookDTO> getBooksByIds(List<Long> ids);

    void updateBookByName(String name, BookDTO book);

    void updateBook(Long id, BookDTO book);

    void deleteBookByName(String name);

    void deleteBook(Long id);

    void addBook(BookDTO book);
}
