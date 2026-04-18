package com.example.backend.event;

import com.example.backend.entity.RescueTask;
import com.example.backend.entity.RescueTaskAssign;
import com.example.backend.mapper.RescueTaskAssignMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class RescueEventListener extends BaseEventListener {

    private final RescueTaskAssignMapper rescueTaskAssignMapper;

    @TransactionalEventListener
    public void onRescueTaskAdd(RescueTaskAddEvent event) {
        notifyWorkers(event.user().getId(), event);
    }

    @TransactionalEventListener
    public void onRescueTaskUpdate(RescueTaskUpdateEvent event) {
        RescueTask task = event.data();
        Set<Long> users = Stream.concat(
                Stream.of(task.getUserId()),
                rescueTaskAssignMapper.queryUserByTask(task.getId()).list(RescueTaskAssign::getUserId)
        ).collect(Collectors.toSet());
        notify(users, event);
    }

    @TransactionalEventListener
    public void onRescueTaskStatus(RescueTaskStatusEvent event) {
        RescueTask task = event.data();
        Set<Long> users = Stream.concat(
                Stream.of(task.getUserId()),
                rescueTaskAssignMapper.queryUserByTask(task.getId()).list(RescueTaskAssign::getUserId)
        ).collect(Collectors.toSet());
        notify(users, event);
        sendEmail(task.getUserId(), event);
    }

    @TransactionalEventListener
    public void onRescueTaskAssign(RescueTaskAssignEvent event) {
        Set<Long> assignees = event.data().stream()
                .map(RescueTaskAssign::getUserId)
                .collect(Collectors.toSet());
        notify(assignees, event);
        sendEmail(assignees, event);
    }
}
