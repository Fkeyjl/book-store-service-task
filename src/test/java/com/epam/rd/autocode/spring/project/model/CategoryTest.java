package com.epam.rd.autocode.spring.project.model;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryTest {

    @Test
    void testCategoryCreation() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Fiction");

        assertThat(category.getId()).isEqualTo(1L);
        assertThat(category.getName()).isEqualTo("Fiction");
    }

    @Test
    void testCategoryWithBooks() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Science Fiction");

        Book book1 = new Book();
        book1.setId(1L);
        book1.setName("Book 1");

        Book book2 = new Book();
        book2.setId(2L);
        book2.setName("Book 2");

        Set<Book> books = new HashSet<>();
        books.add(book1);
        books.add(book2);

        category.setBooks(books);

        assertThat(category.getBooks()).hasSize(2);
        assertThat(category.getBooks()).contains(book1, book2);
    }

    @Test
    void testCategoryEquality() {
        Category category1 = new Category();
        category1.setId(1L);
        category1.setName("Fiction");

        Category category2 = new Category();
        category2.setId(1L);
        category2.setName("Fiction");

        assertThat(category1).isEqualTo(category2);
        assertThat(category1.hashCode()).isEqualTo(category2.hashCode());
    }

    @Test
    void testCategoryDefaultValues() {
        Category category = new Category();

        assertThat(category.getId()).isNull();
        assertThat(category.getName()).isNull();
        assertThat(category.getBooks()).isNull();
    }
}
