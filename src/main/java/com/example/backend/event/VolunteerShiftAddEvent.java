package com.example.backend.event;

import com.example.backend.entity.User;
import com.example.backend.entity.VolunteerShift;

public record VolunteerShiftAddEvent(VolunteerShift shift, User worker) {
}
