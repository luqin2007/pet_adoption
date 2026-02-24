package com.example.backend.handler;

import com.example.backend.bean.Result;
import com.example.backend.util.ServiceException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<Result<Void>> handleServiceException(ServiceException e) {
        e.printStackTrace();
        return Result.wrap(Result.error(e));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleOtherException(Exception e) {
        e.printStackTrace();
        return Result.wrap(Result.error(500, e.getMessage()));
    }
}
