package com.epam.rd.autocode.spring.project.repo;

import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
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

    boolean existsByIsbn(String isbn);
    
    @Query("SELECT b FROM Book b WHERE " +
            "(:language IS NULL OR b.language = :language) AND " +
            "(:ageGroup IS NULL OR b.ageGroup = :ageGroup) AND " +
            "(:minPrice IS NULL OR b.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR b.price <= :maxPrice) AND " +
            "(:searchTerm IS NULL OR LOWER(b.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(b.author) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Book> findByFilters(
            @Param("language") Language language,
            @Param("ageGroup") AgeGroup ageGroup,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("searchTerm") String searchTerm,
            Sort sort);
}
