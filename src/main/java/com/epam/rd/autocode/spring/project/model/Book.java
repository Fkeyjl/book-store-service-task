package com.epam.rd.autocode.spring.project.model;

import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "BOOKS")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private String genre;

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
