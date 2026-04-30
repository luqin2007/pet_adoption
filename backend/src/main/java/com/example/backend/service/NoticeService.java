package com.example.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.component.NoticeSseHub;
import com.example.backend.dto.IdsRequest;
import com.example.backend.dto.NoticeQueryParams;
import com.example.backend.dto.NoticeResponse;
import com.example.backend.dto.PageParams;
import com.example.backend.entity.Notice;
import com.example.backend.entity.User;
import com.example.backend.entity.property.NoticeSource;
import com.example.backend.mapper.NoticeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService extends BaseService<NoticeMapper, Notice> {

    private final NoticeSseHub noticeSseHub;

    public Page<NoticeResponse> getNotices(NoticeQueryParams query, PageParams page) {
        User login = requireLoginUser();
        Page<Notice> result = baseMapper.queryByUser(login.getId(), query).page(page);
        return convertDto(result, NoticeResponse::create);
    }

    public Long countUnread() {
        User login = requireLoginUser();
        return baseMapper.queryUnread(login.getId()).count();
    }

    @Transactional
    public void readNotice(IdsRequest noticeId, boolean isRead) {
        User login = requireLoginUser();
        baseMapper.updateRead(noticeId.idSet(), login.getId(), isRead).update();
    }

    public SseEmitter connect() {
        User login = requireLoginUser();
        return noticeSseHub.connect(login.getId());
    }

    @Transactional
    public List<NoticeResponse> addNotices(Collection<User> receivers, NoticeSource source, String title, String content) {
        List<Notice> notices = receivers.stream()
                .map(receiver -> Notice.create(receiver.getId(), source, title, content, false))
                .toList();
        saveBatch(notices);
        return notices.stream()
                .map(NoticeResponse::create)
                .toList();
    }
}
