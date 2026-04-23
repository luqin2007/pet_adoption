package com.example.backend.service;

import com.example.backend.dto.RescueTaskResponse;
import com.example.backend.entity.RescueTask;
import com.example.backend.entity.User;
import com.example.backend.entity.property.RescueTaskStatus;
import com.example.backend.entity.property.RescueTaskType;
import com.example.backend.entity.property.UserRole;
import com.example.backend.mapper.LocationMapper;
import com.example.backend.mapper.RescueTaskAssignMapper;
import com.example.backend.mapper.RescueTaskMapper;
import com.example.backend.mapper.RescueTaskRecordMapper;
import com.example.backend.util.CustomUserDetails;
import com.example.backend.util.ServiceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RescueTaskServiceUnitTest {

    private RescueTaskMapper rescueTaskMapper;
    private RescueTaskService service;

    @BeforeEach
    void setUp() {
        rescueTaskMapper = mock(RescueTaskMapper.class);
        service = new RescueTaskService(
                mock(LocationMapper.class),
                mock(RescueTaskAssignMapper.class),
                mock(RescueTaskRecordMapper.class));
        ReflectionTestUtils.setField(service, "baseMapper", rescueTaskMapper);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void taskOwnerCanReadOwnRescueTask() {
        login(user(7L, "owner", UserRole.NORMAL.getSetMask()));
        RescueTask task = task(55L, 7L, null);
        when(rescueTaskMapper.requireById(55L)).thenReturn(task);

        RescueTaskResponse response = service.getRescueTask(55L);

        assertEquals(55L, response.getId());
        assertEquals("发现受伤动物", response.getSummary());
    }

    @Test
    void unrelatedNormalUserCannotReadRescueTask() {
        login(user(8L, "stranger", UserRole.NORMAL.getSetMask()));
        RescueTask task = task(55L, 7L, null);
        when(rescueTaskMapper.requireById(55L)).thenReturn(task);

        ServiceException ex = assertThrows(ServiceException.class, () -> service.getRescueTask(55L));

        assertEquals(ServiceException.E_AUTH, ex.getCode());
        assertEquals("exception.auth.denied", ex.getMessage());
    }

    @Test
    void workerCanReadAnyRescueTask() {
        login(user(9L, "worker", UserRole.WORKER.getSetMask()));
        RescueTask task = task(55L, 7L, null);
        when(rescueTaskMapper.requireById(55L)).thenReturn(task);

        assertEquals(55L, service.getRescueTask(55L).getId());
    }

    private static void login(User user) {
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken(new CustomUserDetails(user), null));
    }

    private static User user(Long id, String username, int role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setPassword("{noop}pwd");
        user.setEmail(username + "@example.com");
        user.setRole(role);
        return user;
    }

    private static RescueTask task(Long id, Long userId, Long approveId) {
        return new RescueTask(id,
                null,
                userId,
                approveId,
                "发现受伤动物",
                "需要救助",
                RescueTaskStatus.CREATED,
                RescueTaskType.FIND,
                new Date(),
                new Date());
    }
}
