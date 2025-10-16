package com.epam.rd.autocode.spring.project.utils;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class StringToBigDecimalConverter implements Converter<String, BigDecimal> {
    @Override
    public BigDecimal convert(String source) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }
        try {
            String cleanSource = source.trim().replace(',', '.');
            return new BigDecimal(cleanSource);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
