package com.epam.rd.autocode.spring.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookItemDTO {
    private BookDTO book;
    private int quantity;
    private BigDecimal pricePerUnit;
    private BigDecimal totalPrice;
    public BookItemDTO(BookDTO book, int quantity) {
        this.book = book;
        this.quantity = quantity;
        this.pricePerUnit = book.getPrice();
        this.totalPrice = pricePerUnit.multiply(BigDecimal.valueOf(quantity));
    }
}
