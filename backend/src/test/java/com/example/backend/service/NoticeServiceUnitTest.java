package com.example.backend.service;

import com.baomidou.mybatisplus.annotation.TableField;
import com.example.backend.component.NoticeSseHub;
import com.example.backend.dto.NoticeResponse;
import com.example.backend.entity.Notice;
import com.example.backend.entity.User;
import com.example.backend.entity.property.NoticeSource;
import com.example.backend.entity.property.UserRole;
import com.example.backend.mapper.NoticeMapper;
import com.example.backend.util.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

class NoticeServiceUnitTest {

    private NoticeSseHub noticeSseHub;
    private TestNoticeService service;

    @BeforeEach
    void setUp() {
        noticeSseHub = mock(NoticeSseHub.class);
        service = spy(new TestNoticeService(noticeSseHub));
        ReflectionTestUtils.setField(service, "baseMapper", mock(NoticeMapper.class));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void connectRegistersCurrentUserWithSseHub() {
        login(user(21L, "receiver"));
        SseEmitter emitter = new SseEmitter();
        when(noticeSseHub.connect(21L)).thenReturn(emitter);

        assertSame(emitter, service.connect());

        verify(noticeSseHub).connect(21L);
    }

    @Test
    void addNoticesCreatesUnreadNoticeResponsesForEveryReceiver() {
        doReturn(true).when(service).saveBatch(anyCollection());
        User first = user(21L, "first");
        User second = user(22L, "second");

        List<NoticeResponse> responses = service.addNotices(
                List.of(first, second),
                NoticeSource.PET_ADOPT,
                "领养审核",
                "你的领养申请已更新");

        assertEquals(2, responses.size());
        assertEquals(List.of(21L, 22L), responses.stream().map(NoticeResponse::getReceiverId).toList());
        assertTrue(responses.stream().allMatch(response -> !response.getRead()));
        assertTrue(responses.stream().allMatch(response -> "PET_ADOPT".equals(response.getSource())));
        verify(service).saveBatch(argThat(notices ->
                notices.size() == 2
                        && notices.stream().allMatch(notice -> notice.getRead().equals(Boolean.FALSE))
                        && notices.stream().allMatch(notice -> notice.getCreateTime() != null)));
    }

    @Test
    void readColumnIsEscapedBecauseReadIsReservedInMysqlFamilyDatabases() throws Exception {
        Field read = Notice.class.getDeclaredField("read");

        assertEquals("`read`", read.getAnnotation(TableField.class).value());
    }

    static class TestNoticeService extends NoticeService {
        TestNoticeService(NoticeSseHub noticeSseHub) {
            super(noticeSseHub);
        }

        @Override
        public boolean saveBatch(Collection<Notice> entityList) {
            return true;
        }
    }

    private static void login(User user) {
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken(new CustomUserDetails(user), null));
    }

    private static User user(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setPassword("{noop}pwd");
        user.setEmail(username + "@example.com");
        user.setRole(UserRole.NORMAL.getMask());
        return user;
    }
}
