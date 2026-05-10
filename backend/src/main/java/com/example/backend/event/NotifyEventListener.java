package com.example.backend.event;

import com.example.backend.component.NoticeSseHub;
import com.example.backend.dto.NoticeResponse;
import com.example.backend.entity.User;
import com.example.backend.mapper.UserMapper;
import com.example.backend.service.NoticeService;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class NotifyEventListener {

    private final NoticeService noticeService;
    private final UserMapper userMapper;

    private final NoticeSseHub noticeSseHub;

    @Async
    @EventListener(NotifyEvent.class)
    public void onNoticeSend(NotifyEvent event) {
        Set<Long> ids = event.userIds().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        @SuppressWarnings("unchecked")
        List<User> users = userMapper.selectList(ids, User::getId, User::getEmail);
        List<NoticeResponse> responses = noticeService.addNotices(users, event.source(), event.title(), event.content());
        for (NoticeResponse notice : responses) {
            noticeSseHub.push(notice);
        }
    }
}
