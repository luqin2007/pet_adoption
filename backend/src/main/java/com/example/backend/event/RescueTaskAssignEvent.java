package com.example.backend.event;

import com.example.backend.entity.RescueTask;
import com.example.backend.entity.RescueTaskAssign;
import com.example.backend.entity.User;
import com.example.backend.entity.property.NoticeSource;
import com.example.backend.util.LangHelper;

import java.util.List;

import static com.example.backend.entity.property.NoticeSource.RESCUE_TASK;

/**
 * 救助任务分配
 * - 通知被选中者：同步新的救助任务分配
 *
 * @see com.example.backend.service.RescueTaskService#assignRescueTask(Long, com.example.backend.dto.IdsRequest)
 */
public record RescueTaskAssignEvent(RescueTask task, List<RescueTaskAssign> data, User user) implements INotifyEvent<List<RescueTaskAssign>> {

    @Override
    public NoticeSource getSource() {
        return RESCUE_TASK;
    }

    @Override
    public String buildNotifyTitle(LangHelper langHelper) {
        return langHelper.get("notification.rescue_task_assign.title");
    }

    @Override
    public String buildNotifyContent(LangHelper langHelper, Object... args) {
        return langHelper.get("notification.rescue_task_assign.content", task.getSummary());
    }

    @Override
    public String buildMailTitle(LangHelper langHelper) {
        return langHelper.get("mail.rescue_task_assign.title");
    }

    @Override
    public String buildMailContent(LangHelper langHelper, Object... args) {
        return langHelper.get("mail.rescue_task_assign.content", task.getSummary());
    }
}
