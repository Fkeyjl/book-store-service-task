package com.epam.rd.autocode.spring.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cart implements Serializable {
    private Map<Long, Integer> items = new HashMap<>();
    private int totalQuantity = 0;
    private BigDecimal totalPrice = BigDecimal.ZERO;
}
