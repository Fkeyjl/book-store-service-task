package com.epam.rd.autocode.spring.project.repo;

import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BookRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    void setUp() {
        Book testBook1 = new Book();
        testBook1.setName("Test Book 1");
        testBook1.setAuthor("Author One");
        testBook1.setIsbn("978-0-123456-47-2");
        testBook1.setPrice(new BigDecimal("19.99"));
        testBook1.setLanguage(Language.UKRAINIAN);
        testBook1.setAgeGroup(AgeGroup.ADULT);
        testBook1.setPublicationDate(LocalDate.of(2023, 1, 1));
        testBook1.setPages(200);
        entityManager.persist(testBook1);

        Book testBook2 = new Book();
        testBook2.setName("Test Book 2");
        testBook2.setAuthor("Author Two");
        testBook2.setIsbn("978-0-123456-48-9");
        testBook2.setPrice(new BigDecimal("29.99"));
        testBook2.setLanguage(Language.ENGLISH);
        testBook2.setAgeGroup(AgeGroup.CHILD);
        testBook2.setPublicationDate(LocalDate.of(2022, 6, 15));
        testBook2.setPages(150);
        entityManager.persist(testBook2);

        Book testBook3 = new Book();
        testBook3.setName("Another Book");
        testBook3.setAuthor("Different Author");
        testBook3.setIsbn("978-0-123456-49-6");
        testBook3.setPrice(new BigDecimal("15.50"));
        testBook3.setLanguage(Language.UKRAINIAN);
        testBook3.setAgeGroup(AgeGroup.TEEN);
        testBook3.setPublicationDate(LocalDate.of(2024, 3, 10));
        testBook3.setPages(300);
        entityManager.persist(testBook3);

        entityManager.flush();
    }

    @Test
    void testFindByName_WhenBookExists_ShouldReturnBook() {
        Optional<Book> found = bookRepository.findByName("Test Book 1");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Book 1");
        assertThat(found.get().getAuthor()).isEqualTo("Author One");
    }

    @Test
    void testFindByName_WhenBookDoesNotExist_ShouldReturnEmpty() {
        Optional<Book> found = bookRepository.findByName("Non-existent Book");

        assertThat(found).isEmpty();
    }

    @Test
    void testExistsByIsbn_WhenBookExists_ShouldReturnTrue() {
        boolean exists = bookRepository.existsByIsbn("978-0-123456-47-2");

        assertThat(exists).isTrue();
    }

    @Test
    void testExistsByIsbn_WhenBookDoesNotExist_ShouldReturnFalse() {
        boolean exists = bookRepository.existsByIsbn("978-0-000000-00-0");

        assertThat(exists).isFalse();
    }

    @Test
    void testFindByFilters_FilterByLanguage_ShouldReturnMatchingBooks() {
        List<Book> found = bookRepository.findByFilters(
                null, Language.UKRAINIAN, null, null, null, null, Sort.unsorted()
        );

        assertThat(found).hasSize(2);
        assertThat(found).extracting(Book::getLanguage)
                .containsOnly(Language.UKRAINIAN);
    }

    @Test
    void testFindByFilters_FilterByAgeGroup_ShouldReturnMatchingBooks() {
        List<Book> found = bookRepository.findByFilters(
                null, null, AgeGroup.ADULT, null, null, null, Sort.unsorted()
        );

        assertThat(found).hasSize(12);
        assertThat(found.get(0).getAgeGroup()).isEqualTo(AgeGroup.ADULT);
    }

    @Test
    void testFindByFilters_FilterByPriceRange_ShouldReturnMatchingBooks() {
        List<Book> found = bookRepository.findByFilters(
                null, null, null, new BigDecimal("15.00"), new BigDecimal("20.00"), null, Sort.unsorted()
        );

        assertThat(found).hasSize(7);
        assertThat(found).allMatch(book ->
                book.getPrice().compareTo(new BigDecimal("15.00")) >= 0 &&
                        book.getPrice().compareTo(new BigDecimal("20.00")) <= 0
        );
    }

    @Test
    void testFindByFilters_SearchByName_ShouldReturnMatchingBooks() {
        List<Book> found = bookRepository.findByFilters(
                null, null, null, null, null, "Test", Sort.unsorted()
        );

        assertThat(found).hasSize(2);
        assertThat(found).extracting(Book::getName)
                .allMatch(name -> name.toLowerCase().contains("test"));
    }

    @Test
    void testFindByFilters_SearchByAuthor_ShouldReturnMatchingBooks() {
        List<Book> found = bookRepository.findByFilters(
                null, null, null, null, null, "Author", Sort.unsorted()
        );

        assertThat(found).hasSize(3);
    }

    @Test
    void testFindByFilters_CombinedFilters_ShouldReturnMatchingBooks() {
        List<Book> found = bookRepository.findByFilters(
                null, Language.UKRAINIAN, AgeGroup.ADULT,
                new BigDecimal("10.00"), new BigDecimal("30.00"),
                null, Sort.unsorted()
        );

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getName()).isEqualTo("Test Book 1");
    }

    @Test
    void testFindByFilters_WithSort_ShouldReturnSortedBooks() {
        List<Book> found = bookRepository.findByFilters(
                null, null, null, null, null, null,
                Sort.by(Sort.Direction.DESC, "price")
        );

        assertThat(found).hasSize(24);
        assertThat(found.get(0).getPrice()).isGreaterThan(found.get(1).getPrice());
        assertThat(found.get(1).getPrice()).isGreaterThan(found.get(2).getPrice());
    }

    @Test
    void testFindByFilters_NoFilters_ShouldReturnAllBooks() {
        List<Book> found = bookRepository.findByFilters(
                null, null, null, null, null, null, Sort.unsorted()
        );

        assertThat(found).hasSize(24);
    }
}
