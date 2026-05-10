package com.example.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.dto.*;
import com.example.backend.service.NoticeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 站内信模块<br>
 * - 查询站内信：getNoticeList ( √ × )<br>
 * - 获取未读数量：getUnreadCount ( √ × )<br>
 * - 标记已读：read ( √ × )<br>
 * - 标记未读：unread ( √ × )<br>
 * - 客户端连接：connect ( √ × )
 */
@Validated
@RestController
@RequestMapping("/api/v1/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping({"", "/"})
    public Result<Page<NoticeResponse>> getNoticeList(@Valid NoticeQueryParams query, PageParams page) {
        Page<NoticeResponse> response = noticeService.getNotices(query, page);
        return Result.success(response);
    }

    @PatchMapping("/read")
    public Result<Void> read(IdsRequest noticeIds) {
        noticeService.readNotice(noticeIds, true);
        return Result.success();
    }

    @PatchMapping("/unread")
    public Result<Void> unread(IdsRequest noticeIds) {
        noticeService.readNotice(noticeIds, false);
        return Result.success();
    }

    @GetMapping("/unread")
    public Result<Long> getUnreadCount() {
        Long response = noticeService.countUnread();
        return Result.success(response);
    }

    @GetMapping("/connect")
    public SseEmitter subscribe() {
        return noticeService.connect();
    }
}
