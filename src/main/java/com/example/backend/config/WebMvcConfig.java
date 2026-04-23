package com.example.backend.config;

import com.example.backend.component.RequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

@Configuration
@ControllerAdvice
@RequiredArgsConstructor
public class WebMvcConfig {

    private final RequestValidator requestValidator;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        Object target = binder.getTarget();
        if (target != null && requestValidator.supports(target.getClass())) {
            binder.addValidators(requestValidator);
        }
    }
}
