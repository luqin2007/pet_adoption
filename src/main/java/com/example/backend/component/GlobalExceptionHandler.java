package com.example.backend.component;

import com.example.backend.dto.Result;
import com.example.backend.util.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.text.DateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyMMdd");

    private static int err_index = 0;

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<Result<Void>> handleServiceException(ServiceException e) {
        LOGGER.error("Service Exception", e);
        return Result.wrap(Result.error(e));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleOtherException(Exception e) {
        String errorCode = FORMAT.format(Instant.now()) + String.format("%03d", ++err_index);
        LOGGER.error("Other Exception {}", errorCode, e);
        return Result.wrap(Result.error(500, "发生错误 " + errorCode));
    }
}
