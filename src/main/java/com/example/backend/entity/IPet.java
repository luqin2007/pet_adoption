package com.example.backend.entity;

import com.example.backend.util.StringUtils;

public interface IPet extends IId {

    String getSex();

    String getType();

    String getBreed();

    default boolean matchPet(IPet other) {
        return equalOrNull(getSex(), other.getSex())
                && equalOrNull(getType(), other.getType())
                && equalOrNull(getBreed(), other.getBreed());
    }

    private boolean equalOrNull(String str1, String str2) {
        return !StringUtils.hasText(str1) || !StringUtils.hasText(str2) || str1.equalsIgnoreCase(str2);
    }
}
