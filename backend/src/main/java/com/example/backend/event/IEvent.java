package com.example.backend.event;

import com.example.backend.entity.User;

public interface IEvent<T> {

    T data();

    User user();
}
