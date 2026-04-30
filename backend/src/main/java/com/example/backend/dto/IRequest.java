package com.example.backend.dto;

public interface IRequest {

    @SuppressWarnings("unchecked")
    default <T> T self() {
        return (T) this;
    }
}
