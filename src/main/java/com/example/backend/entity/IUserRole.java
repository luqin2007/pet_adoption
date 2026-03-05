package com.example.backend.entity;

import static com.example.backend.util.AuthUtils.*;

public interface IUserRole {

    Integer getRole();

    void setRole(Integer role);

    default boolean isVolunteer() {
        Integer role = getRole();
        return role != null && (role & MASK_VOLUNTEER) == MASK_VOLUNTEER;
    }

    default boolean isWorker() {
        Integer role = getRole();
        return role != null && (role & MASK_WORKER) == MASK_WORKER;
    }

    default boolean isDonor() {
        Integer role = getRole();
        return role != null && (role & MASK_DONOR) == MASK_DONOR;
    }

    default boolean isVeterinarian() {
        Integer role = getRole();
        return role != null && (role & MASK_VETERINARIAN) == MASK_VETERINARIAN;
    }

    default boolean isAdmin() {
        Integer role = getRole();
        return role != null && (role & MASK_ADMIN) == MASK_ADMIN;
    }
}
