package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.CategoryDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.model.Category;
import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.repo.CategoryRepository;
import com.epam.rd.autocode.spring.project.service.impl.BookServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private CategoryRepository categoryRepository;
    
    @Mock
    private CategoryService categoryService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book testBook;
    private BookDTO testBookDTO;

    @BeforeEach
    void setUp() {
        testBook = new Book();
        testBook.setId(1L);
        testBook.setName("Test Book");
        testBook.setAuthor("Test Author");
        testBook.setIsbn("978-0-123456-47-2");
        testBook.setPrice(new BigDecimal("19.99"));
        testBook.setLanguage(Language.UKRAINIAN);
        testBook.setAgeGroup(AgeGroup.ADULT);
        testBook.setPublicationDate(LocalDate.now());
        testBook.setPages(200);

        testBookDTO = new BookDTO();
        testBookDTO.setId(1L);
        testBookDTO.setName("Test Book");
        testBookDTO.setAuthor("Test Author");
        testBookDTO.setIsbn("978-0-123456-47-2");
        testBookDTO.setPrice(new BigDecimal("19.99"));
        testBookDTO.setLanguage(Language.UKRAINIAN);
        testBookDTO.setAgeGroup(AgeGroup.ADULT);
        testBookDTO.setPublicationDate(LocalDate.now());
        testBookDTO.setPages(200);
    }

    @Test
    void testGetAllBooks_ShouldReturnAllBooks() {
        List<Book> books = Arrays.asList(testBook);
        when(bookRepository.findAllWithCategories()).thenReturn(books);
        when(modelMapper.map(any(Book.class), eq(BookDTO.class))).thenReturn(testBookDTO);

        List<BookDTO> result = bookService.getAllBooks();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Test Book");
        verify(bookRepository, times(1)).findAllWithCategories();
    }

    @Test
    void testGetBookById_WhenBookExists_ShouldReturnBook() {
        when(bookRepository.findByIdWithCategories(1L)).thenReturn(Optional.of(testBook));
        when(modelMapper.map(any(Book.class), eq(BookDTO.class))).thenReturn(testBookDTO);

        BookDTO result = bookService.getBookById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Book");
        verify(bookRepository, times(1)).findByIdWithCategories(1L);
    }

    @Test
    void testGetBookById_WhenBookDoesNotExist_ShouldThrowException() {
        when(bookRepository.findByIdWithCategories(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getBookById(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Book not found with id: 1");
    }

    @Test
    void testGetBookByName_WhenBookExists_ShouldReturnBook() {
        when(bookRepository.findByNameWithCategories("Test Book")).thenReturn(Optional.of(testBook));
        when(modelMapper.map(any(Book.class), eq(BookDTO.class))).thenReturn(testBookDTO);

        BookDTO result = bookService.getBookByName("Test Book");

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Book");
        verify(bookRepository, times(1)).findByNameWithCategories("Test Book");
    }

    @Test
    void testAddBook_WhenIsbnIsUnique_ShouldSaveBook() {
        when(bookRepository.existsByIsbn(testBookDTO.getIsbn())).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        bookService.addBook(testBookDTO);

        verify(bookRepository, times(1)).existsByIsbn(testBookDTO.getIsbn());
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void testAddBook_WhenIsbnAlreadyExists_ShouldThrowException() {
        when(bookRepository.existsByIsbn(testBookDTO.getIsbn())).thenReturn(true);

        assertThatThrownBy(() -> bookService.addBook(testBookDTO))
                .isInstanceOf(AlreadyExistException.class)
                .hasMessageContaining("Book with ISBN")
                .hasMessageContaining("already exists");

        verify(bookRepository, times(1)).existsByIsbn(testBookDTO.getIsbn());
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void testDeleteBook_ShouldDeleteBook() {
        doNothing().when(bookRepository).deleteById(1L);

        bookService.deleteBook(1L);

        verify(bookRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteBookByName_WhenBookExists_ShouldDeleteBook() {
        when(bookRepository.findByNameWithCategories("Test Book")).thenReturn(Optional.of(testBook));
        doNothing().when(bookRepository).delete(testBook);

        bookService.deleteBookByName("Test Book");

        verify(bookRepository, times(1)).findByNameWithCategories("Test Book");
        verify(bookRepository, times(1)).delete(testBook);
    }

    @Test
    void testDeleteBookByName_WhenBookDoesNotExist_ShouldThrowException() {
        when(bookRepository.findByNameWithCategories("Test Book")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.deleteBookByName("Test Book"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Book not found with name: Test Book");
    }

    @Test
    void testGetFilteredAndSortedBooks_WithFilters_ShouldReturnFilteredBooks() {
        List<Book> books = Arrays.asList(testBook);
        when(bookRepository.findByFilters(
                isNull(),
                eq(Language.UKRAINIAN),
                eq(AgeGroup.ADULT),
                any(BigDecimal.class),
                any(BigDecimal.class),
                isNull(),
                any(Sort.class)
        )).thenReturn(books);
        when(modelMapper.map(any(Book.class), eq(BookDTO.class))).thenReturn(testBookDTO);

        List<BookDTO> result = bookService.getFilteredAndSortedBooks(
                null,
                Language.UKRAINIAN,
                AgeGroup.ADULT,
                new BigDecimal("10.00"),
                new BigDecimal("30.00"),
                null,
                Sort.by("price")
        );

        assertThat(result).hasSize(1);
        verify(bookRepository, times(1)).findByFilters(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testUpdateBook_WhenBookExists_ShouldUpdateBook() {
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setId(1L);
        categoryDTO.setName("Fiction");
        
        Category category = new Category();
        category.setId(1L);
        category.setName("Fiction");

        Set<CategoryDTO> categoriesDTO = new HashSet<>();
        categoriesDTO.add(categoryDTO);
        testBookDTO.setCategories(categoriesDTO);

        when(bookRepository.findByIdWithCategories(1L)).thenReturn(Optional.of(testBook));
        when(categoryService.resolveCategoriesForUpdate(anySet())).thenReturn(new HashSet<>(Arrays.asList(category)));
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        bookService.updateBook(1L, testBookDTO);

        verify(bookRepository, times(1)).findByIdWithCategories(1L);
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void testUpdateBook_WhenBookDoesNotExist_ShouldThrowException() {
        when(bookRepository.findByIdWithCategories(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.updateBook(1L, testBookDTO))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Book not found with id: 1");
    }
}
