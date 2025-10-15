package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.BookSummaryDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.model.Category;
import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.service.BookService;
import com.epam.rd.autocode.spring.project.service.CategoryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;
    private final CategoryService categoryService;
    private final ModelMapper modelMapper;

    @Override
    public List<BookDTO> getAllBooks() {
        return bookRepository.findAll().stream()
                .map(book -> modelMapper.map(book, BookDTO.class))
                .toList();
    }

    @Override
    public List<BookDTO> getFilteredAndSortedBooks(
            Language language,
            AgeGroup ageGroup,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String searchTerm,
            Sort sort) {
        
        Sort sortToUse = sort != null ? sort : Sort.unsorted();
        
        List<Book> books = bookRepository.findByFilters(
                language, 
                ageGroup, 
                minPrice, 
                maxPrice,
                searchTerm,
                sortToUse
        );
        
        return books.stream()
                .map(book -> modelMapper.map(book, BookDTO.class))
                .toList();
    }

    @Override
    public Page<BookSummaryDTO> getNewestBooksPaged(int page, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "publicationDate");
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Book> booksPage = bookRepository.findAll(pageable);
        return booksPage.map(book -> modelMapper.map(book, BookSummaryDTO.class));
    }

    @Override
    public BookDTO getBookByName(String name) {
        Book book = bookRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Book not found with name: " + name));
        return modelMapper.map(book, BookDTO.class);
    }

    public BookDTO getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book not found with id: " + id));
        return modelMapper.map(book, BookDTO.class);
    }

    @Override
    public List<BookDTO> getBooksByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return bookRepository.findAllById(ids).stream()
                .map(book -> modelMapper.map(book, BookDTO.class))
                .toList();
    }

    @Override
    @Transactional
    public void updateBookByName(String name, BookDTO book) {
        Book existingBook = bookRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Book not found with name: " + name));
        modelMapper.map(book, existingBook);
        Set<Category> newCategories = categoryService.resolveCategoriesForUpdate(book.getCategories());
        existingBook.setCategories(newCategories);
        bookRepository.save(existingBook);
    }

    @Override
    @Transactional
    public void updateBook(Long id, BookDTO book) {
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book not found with id: " + id));
        modelMapper.map(book, existingBook);
        Set<Category> newCategories = categoryService.resolveCategoriesForUpdate(book.getCategories());
        existingBook.setCategories(newCategories);
        bookRepository.save(existingBook);
    }

    @Override
    public void deleteBookByName(String name) {
        Book book = bookRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Book not found with name: " + name));
        bookRepository.delete(book);
    }

    @Override
    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void addBook(BookDTO bookDTO) {
        if (bookRepository.existsByIsbn(bookDTO.getIsbn())) {
            throw new AlreadyExistException("Book with ISBN " + bookDTO.getIsbn() + " already exists.");
        }
        Book newBook = new Book();
        modelMapper.map(bookDTO, newBook);
        bookRepository.save(newBook);
    }
}
