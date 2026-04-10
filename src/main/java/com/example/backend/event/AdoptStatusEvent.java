package com.example.backend.event;

import com.example.backend.entity.Adopt;
import com.example.backend.entity.property.AdoptBreadingStatus;

/**
 * 领养状态变更事件
 */
public record AdoptStatusEvent(Adopt adopt, AdoptBreadingStatus oldStatus) {
}
