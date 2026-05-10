package com.example.backend.event;

import com.example.backend.entity.FollowRecord;
import com.example.backend.entity.User;
import com.example.backend.entity.property.NoticeSource;
import com.example.backend.util.LangHelper;

/**
 * 添加跟踪任务记录事件
 * - 通知工作人员：回访结束
 * - 通知领养人：回访结束
 *
 * @see com.example.backend.service.AdoptBreadingService#addFollowRecord(Long, com.example.backend.dto.FollowRecordAddRequest)
 */
public record FollowRecordEvent(FollowRecord data, User user) implements INotifyEvent<FollowRecord> {
    @Override
    public NoticeSource getSource() {
        return NoticeSource.FOLLOW_TASK;
    }

    @Override
    public String buildNotifyTitle(LangHelper langHelper) {
        return langHelper.get("notification.follow_record_add.title");
    }

    @Override
    public String buildNotifyContent(LangHelper langHelper, Object... args) {
        return langHelper.get("notification.follow_record_add.content");
    }
}
