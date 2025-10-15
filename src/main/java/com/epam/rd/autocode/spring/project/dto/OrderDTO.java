package com.epam.rd.autocode.spring.project.dto;

import com.epam.rd.autocode.spring.project.model.enums.Status;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO{
    private Long id;

    @NotBlank
    @Email
    private String userEmail;

    @NotNull
    @PastOrPresent
    private LocalDateTime orderDate;

    @NotNull
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Price must have maximum 10 integer digits and 2 decimal places")
    private BigDecimal price;

    @NotNull
    private List<BookItemDTO> bookItems;

    @NotNull
    private Status status;
}
