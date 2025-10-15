package com.epam.rd.autocode.spring.project.model;

import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"categories", "bookItems"})
@ToString(exclude = "bookItems")
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "book_categories",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories;

    @OneToMany(mappedBy = "book", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<BookItem> bookItems;

    @Column
    private String isbn;

    @Enumerated(EnumType.STRING)
    private AgeGroup ageGroup;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "publication_year")
    private LocalDate publicationDate;

    @Column
    private String author;

    @Column(name = "number_of_pages")
    private Integer pages;

    @Column
    private String characteristics;

    @Column
    private String description;

    @Enumerated(EnumType.STRING)
    private Language language;
}
