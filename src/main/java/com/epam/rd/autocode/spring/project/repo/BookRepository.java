package com.epam.rd.autocode.spring.project.repo;

import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findByName(String name);

    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.categories WHERE b.name = :name")
    Optional<Book> findByNameWithCategories(@Param("name") String name);

    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.categories WHERE b.id = :id")
    Optional<Book> findByIdWithCategories(@Param("id") Long id);

    @Query("SELECT DISTINCT b FROM Book b LEFT JOIN FETCH b.categories WHERE b.id IN :ids")
    List<Book> findAllByIdWithCategories(@Param("ids") List<Long> ids);

    @Query("SELECT DISTINCT b FROM Book b LEFT JOIN FETCH b.categories")
    List<Book> findAllWithCategories();

    boolean existsByIsbn(String isbn);

    @Query("SELECT DISTINCT b FROM Book b LEFT JOIN FETCH b.categories c WHERE " +
            "(:categoryId IS NULL OR c.id = :categoryId) AND " +
            "(:language IS NULL OR b.language = :language) AND " +
            "(:ageGroup IS NULL OR b.ageGroup = :ageGroup) AND " +
            "(:minPrice IS NULL OR b.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR b.price <= :maxPrice) AND " +
            "(:searchTerm IS NULL OR LOWER(b.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(b.author) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Book> findByFilters(
            @Param("categoryId") Long categoryId,
            @Param("language") Language language,
            @Param("ageGroup") AgeGroup ageGroup,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("searchTerm") String searchTerm,
            Sort sort);

    @Query(value = "SELECT DISTINCT b FROM Book b LEFT JOIN b.categories c WHERE " +
            "(:categoryId IS NULL OR c.id = :categoryId) AND " +
            "(:language IS NULL OR b.language = :language) AND " +
            "(:ageGroup IS NULL OR b.ageGroup = :ageGroup) AND " +
            "(:minPrice IS NULL OR b.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR b.price <= :maxPrice) AND " +
            "(:searchTerm IS NULL OR LOWER(b.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(b.author) LIKE LOWER(CONCAT('%', :searchTerm, '%')))",
            countQuery = "SELECT COUNT(DISTINCT b.id) FROM Book b LEFT JOIN b.categories c WHERE " +
            "(:categoryId IS NULL OR c.id = :categoryId) AND " +
            "(:language IS NULL OR b.language = :language) AND " +
            "(:ageGroup IS NULL OR b.ageGroup = :ageGroup) AND " +
            "(:minPrice IS NULL OR b.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR b.price <= :maxPrice) AND " +
            "(:searchTerm IS NULL OR LOWER(b.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(b.author) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Book> findByFiltersPaged(@Param("categoryId") Long categoryId,
                                   @Param("language") Language language,
                                   @Param("ageGroup") AgeGroup ageGroup,
                                   @Param("minPrice") BigDecimal minPrice,
                                   @Param("maxPrice") BigDecimal maxPrice,
                                   @Param("searchTerm") String searchTerm,
                                   Pageable pageable);
}
