package com.example.backend.entity.property;

import com.example.backend.util.ServiceException;

import java.util.Locale;

public enum ActionStatus {
    WAITING,
    WORKING,
    SUCCESS,
    FAILED;

    public static ActionStatus get(String name) {
        try {
            return ActionStatus.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw ServiceException.invalidate("exception.invalidate.status");
        }
    }
}
