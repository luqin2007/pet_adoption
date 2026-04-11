package com.example.backend.config;

import com.example.backend.component.RequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Configuration
@RestControllerAdvice
@RequiredArgsConstructor
public class WebMvcConfig {

    private final RequestValidator requestValidator;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.addValidators(requestValidator);
    }
}
