package com.example.backend.event;

import com.example.backend.entity.VolunteerShift;
import com.example.backend.entity.VolunteerShiftStatusRecord;

public record VolunteerShiftStatusEvent(VolunteerShift shift, VolunteerShiftStatusRecord record) {
}
