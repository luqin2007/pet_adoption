package com.example.backend.component;

import com.example.backend.dto.Result;
import com.example.backend.util.ServiceException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@RestControllerAdvice
public class GlobalExceptionHandler implements AsyncUncaughtExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyMMdd");
    private static final AtomicInteger ERR_INDEX = new AtomicInteger(0);
    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<Result<Void>> handleServiceException(ServiceException e) {
        LOGGER.error("Service Exception", e);
        String message = messageSource.getMessage(e.getMessage(), null, e.getMessage(), LocaleContextHolder.getLocale());
        return Result.wrap(Result.error(e.getCode(), message));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Void>> handleConstraintViolationException(ConstraintViolationException e) {
        String messageCode = e.getConstraintViolations().stream()
                .findFirst()
                .map(ConstraintViolation::getMessage)
                .orElse("request.dependency");
        String message = messageSource.getMessage(messageCode, null, messageCode, LocaleContextHolder.getLocale());
        return Result.wrap(Result.error(ServiceException.E_INVALIDATE, message));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String messageCode = Optional.ofNullable(e.getBindingResult().getFieldError())
                .map(error -> Optional.ofNullable(error.getDefaultMessage()).orElse("request.dependency"))
                .orElse("request.dependency");
        String message = messageSource.getMessage(messageCode, null, messageCode, LocaleContextHolder.getLocale());
        return Result.wrap(Result.error(ServiceException.E_INVALIDATE, message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleOtherException(Exception e) {
        String errorCode = FORMAT.format(LocalDate.now()) + String.format("%03d", ERR_INDEX.getAndIncrement());
        LOGGER.error("Other Exception {}", errorCode, e);
        if (ERR_INDEX.get() > 990)
            ERR_INDEX.set(0); // 重置
        return Result.wrap(Result.error(500, "发生错误 " + errorCode));
    }

    @Override
    public void handleUncaughtException(Throwable ex, Method method, @Nullable Object... params) {
        if (ex instanceof ServiceException e) {
            handleServiceException(e);
        } else if (ex instanceof Exception e) {
            handleOtherException(e);
        } else {
            // Error 直接崩溃
            LOGGER.error("Uncaught Exception", ex);
        }
    }
}
