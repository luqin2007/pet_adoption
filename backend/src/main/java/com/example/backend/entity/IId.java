package com.example.backend.entity;

public interface IId {

    Long getId();

    default boolean is(Long id) {
        return this.getId().equals(id);
    }
}
