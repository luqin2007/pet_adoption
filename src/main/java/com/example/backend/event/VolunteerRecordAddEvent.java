package com.example.backend.event;

import com.example.backend.entity.User;
import com.example.backend.entity.VolunteerServiceRecord;
import com.example.backend.entity.VolunteerShift;

public record VolunteerRecordAddEvent(VolunteerShift shift, VolunteerServiceRecord record, User user) {
}
