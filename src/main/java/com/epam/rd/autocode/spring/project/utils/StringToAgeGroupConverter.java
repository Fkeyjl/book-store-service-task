package com.epam.rd.autocode.spring.project.utils;

import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToAgeGroupConverter implements Converter<String, AgeGroup> {
    @Override
    public AgeGroup convert(String source) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }
        try {
            return AgeGroup.valueOf(source.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
