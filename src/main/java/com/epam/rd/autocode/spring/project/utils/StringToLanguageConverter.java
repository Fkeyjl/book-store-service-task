package com.epam.rd.autocode.spring.project.utils;

import com.epam.rd.autocode.spring.project.model.enums.Language;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToLanguageConverter implements Converter<String, Language> {

    @Override
    public Language convert(String source) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }
        try {
            return Language.valueOf(source.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
