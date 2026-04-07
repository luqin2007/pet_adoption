package com.example.backend.dto;

public interface IRequest {

    default <T> T self() {
        return (T) this;
    }
}
