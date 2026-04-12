package com.example.backend.component;

import com.example.backend.dto.Result;
import com.example.backend.util.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@RestControllerAdvice
public class GlobalExceptionHandler {

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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleOtherException(Exception e) {
        String errorCode = FORMAT.format(Instant.now()) + String.format("%03d", ERR_INDEX.getAndIncrement());
        LOGGER.error("Other Exception {}", errorCode, e);
        return Result.wrap(Result.error(500, "发生错误 " + errorCode));
    }
}
