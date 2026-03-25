package com.example.backend.entity;

import com.example.backend.util.Bits;

import static com.example.backend.util.C.*;

public interface IUserRole {

    Integer getRole();

    void setRole(Integer role);

    default boolean isVolunteer() {
        return Bits.match(getRole(), USER_ROLE_MASK_VOLUNTEER);
    }

    default boolean isWorker() {
        return Bits.match(getRole(), USER_ROLE_MASK_WORKER);
    }

    default boolean isDonor() {
        return Bits.match(getRole(), USER_ROLE_MASK_DONOR);
    }

    default boolean isDoctor() {
        return Bits.match(getRole(), USER_ROLE_MASK_DOCTOR);
    }

    default boolean isAdmin() {
        return Bits.match(getRole(), USER_ROLE_MASK_ADMIN);
    }
}
