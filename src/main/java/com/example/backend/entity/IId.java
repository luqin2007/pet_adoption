package com.example.backend.entity;

public interface IId {

    Long getId();

    default boolean is(IId other) {
        return getClass() == other.getClass() && this.getId().equals(other.getId());
    }

    default boolean is(Long id) {
        return this.getId().equals(id);
    }
}
