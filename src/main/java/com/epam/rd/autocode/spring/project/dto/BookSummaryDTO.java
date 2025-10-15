package com.epam.rd.autocode.spring.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookSummaryDTO {
    private Long id;
    private String name;
    private String author;
    private LocalDate publicationDate;
    private BigDecimal price;
}
