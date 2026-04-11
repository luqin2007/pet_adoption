package com.example.backend.dto;

import com.example.backend.util.ServiceException;
import org.springframework.http.ResponseEntity;

public record Result<T>(int code, T data, String message) {

    public static <T> Result<T> success(T data) {
        return new Result<>(200, data, "success");
    }

    public static Result<Void> success() {
        return new Result<>(200, null, "success");
    }

    public static <T> Result<T> error(ServiceException exception) {
        return new Result<>(exception.getCode(), null, exception.getMessage());
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, null, message);
    }

    public static <T> ResponseEntity<Result<T>> wrap(Result<T> result) {
        if (result.code() != 200) { // error
            return ResponseEntity.status(result.code()).body(result);
        }

        return ResponseEntity.ok(result);
    }
}
