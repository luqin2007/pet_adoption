package com.example.backend.event;

import com.example.backend.entity.User;
import com.example.backend.entity.VolunteerServiceRecord;
import com.example.backend.entity.property.VolunteerRecordStatus;

public record VolunteerRecordStatusEvent(VolunteerServiceRecord record, User user, VolunteerRecordStatus oldStatus) {
}
