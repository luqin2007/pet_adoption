package com.example.backend.entity;

import com.example.backend.entity.property.UserRole;

public interface IUserRole {

    Integer getRole();

    void setRole(Integer role);

    default boolean isVolunteer() {
        return UserRole.VOLUNTEER.match(getRole());
    }

    default boolean isWorker() {
        return UserRole.WORKER.match(getRole());
    }

    default boolean isDonor() {
        return UserRole.DONOR.match(getRole());
    }

    default boolean isDoctor() {
        return UserRole.DOCTOR.match(getRole());
    }

    default boolean isAdmin() {
        return UserRole.ADMIN.match(getRole());
    }
}
