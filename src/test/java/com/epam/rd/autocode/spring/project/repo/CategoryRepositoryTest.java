package com.epam.rd.autocode.spring.project.repo;

import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.model.Category;
import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CategoryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category fictionCategory;
    private Category scienceCategory;

    @BeforeEach
    void setUp() {
        fictionCategory = new Category();
        fictionCategory.setName("Fiction");
        entityManager.persist(fictionCategory);

        scienceCategory = new Category();
        scienceCategory.setName("Science");
        entityManager.persist(scienceCategory);

        entityManager.flush();
    }

    @Test
    void testFindByName_WhenCategoryExists_ShouldReturnCategory() {
        Optional<Category> found = categoryRepository.findByName("Fiction");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Fiction");
    }

    @Test
    void testFindByName_WhenCategoryDoesNotExist_ShouldReturnEmpty() {
        Optional<Category> found = categoryRepository.findByName("NonExistent");

        assertThat(found).isEmpty();
    }

    @Test
    void testFindByName_CaseSensitive_ShouldReturnEmpty() {
        Optional<Category> found = categoryRepository.findByName("fiction");

        assertThat(found).isEmpty();
    }

    @Test
    void testExistsByName_WhenCategoryExists_ShouldReturnTrue() {
        boolean exists = categoryRepository.existsByName("Fiction");

        assertThat(exists).isTrue();
    }

    @Test
    void testExistsByName_WhenCategoryDoesNotExist_ShouldReturnFalse() {
        boolean exists = categoryRepository.existsByName("NonExistent");

        assertThat(exists).isFalse();
    }

    @Test
    void testFindAll_ShouldReturnAllCategories() {
        List<Category> categories = categoryRepository.findAll();

        assertThat(categories).hasSize(9);
    }

    @Test
    void testSaveCategory_ShouldPersistCategory() {
        Category newCategory = new Category();
        newCategory.setName("History");

        Category saved = categoryRepository.save(newCategory);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("History");
    }

    @Test
    void testUpdateCategory_ShouldUpdateCategoryName() {
        Category category = categoryRepository.findByName("Fiction").orElseThrow();
        category.setName("Fiction & Literature");

        Category updated = categoryRepository.save(category);

        assertThat(updated.getName()).isEqualTo("Fiction & Literature");
    }

    @Test
    void testDeleteCategory_ShouldRemoveCategory() {
        Category category = categoryRepository.findByName("Fiction").orElseThrow();
        Long categoryId = category.getId();

        categoryRepository.delete(category);
        entityManager.flush();

        Optional<Category> deleted = categoryRepository.findById(categoryId);
        assertThat(deleted).isEmpty();
    }

    @Test
    void testCategoryWithBooks_ShouldMaintainRelationship() {
        Category category = categoryRepository.findByName("Fiction").orElseThrow();

        Book book1 = new Book();
        book1.setName("Book 1");
        book1.setIsbn("978-0-123456-47-2");
        book1.setAuthor("Author 1");
        book1.setPrice(new BigDecimal("19.99"));
        book1.setPublicationDate(LocalDate.now());
        book1.setPages(200);
        book1.setLanguage(Language.ENGLISH);
        book1.setAgeGroup(AgeGroup.ADULT);
        book1.setCategories(Set.of(category));
        entityManager.persist(book1);

        Book book2 = new Book();
        book2.setName("Book 2");
        book2.setIsbn("978-0-123456-48-9");
        book2.setAuthor("Author 2");
        book2.setPrice(new BigDecimal("24.99"));
        book2.setPublicationDate(LocalDate.now());
        book2.setPages(300);
        book2.setLanguage(Language.ENGLISH);
        book2.setAgeGroup(AgeGroup.ADULT);
        book2.setCategories(Set.of(category));
        entityManager.persist(book2);

        entityManager.flush();
        entityManager.clear();

        Category foundCategory = categoryRepository.findById(category.getId()).orElseThrow();
        assertThat(foundCategory.getBooks()).hasSize(2);
        assertThat(foundCategory.getBooks()).extracting(Book::getName)
                .containsExactlyInAnyOrder("Book 1", "Book 2");
    }

    @Test
    void testFindById_WhenCategoryExists_ShouldReturnCategory() {
        Category category = categoryRepository.findByName("Science").orElseThrow();
        Long categoryId = category.getId();

        Optional<Category> found = categoryRepository.findById(categoryId);

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Science");
    }

    @Test
    void testFindById_WhenCategoryDoesNotExist_ShouldReturnEmpty() {
        Optional<Category> found = categoryRepository.findById(999L);

        assertThat(found).isEmpty();
    }
}
