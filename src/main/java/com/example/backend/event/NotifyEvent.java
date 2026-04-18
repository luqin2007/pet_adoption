package com.example.backend.event;

import com.example.backend.entity.property.NoticeSource;

import java.util.Collection;

public record NotifyEvent(Collection<Long> userIds, NoticeSource source, String title, String content) {
}
