package com.example.backend.util;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LangHelper {

    private final MessageSource messages;

    public String get(String code, Object... args) {
        return messages.getMessage(code, args, LocaleContextHolder.getLocale());
    }
}
