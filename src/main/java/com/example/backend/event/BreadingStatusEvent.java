package com.example.backend.event;

import com.example.backend.entity.Breading;
import com.example.backend.entity.property.AdoptBreadingStatus;

/**
 * 寄养状态变更事件
 */
public record BreadingStatusEvent(Breading breading, AdoptBreadingStatus oldStatus) {
}
