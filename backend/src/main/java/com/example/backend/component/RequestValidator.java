package com.example.backend.component;


import com.example.backend.dto.IValidatedRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * 自定义请求体验证
 */
@Component
@SuppressWarnings("NullableProblems")
public class RequestValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return IValidatedRequest.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        IValidatedRequest request = (IValidatedRequest) target;
        request.validate(errors);
    }
}
