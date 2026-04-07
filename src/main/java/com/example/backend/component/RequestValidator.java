package com.example.backend.component;


import com.example.backend.dto.IRequestValidate;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * 自定义请求体验证
 */
@Component
public class RequestValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return IRequestValidate.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        IRequestValidate request = (IRequestValidate) target;
        request.validate(errors);
    }
}
