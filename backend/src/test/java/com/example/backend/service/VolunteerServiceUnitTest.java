package com.example.backend.service;

import com.example.backend.dto.VolunteerRewardResponse;
import com.example.backend.entity.User;
import com.example.backend.entity.VolunteerReward;
import com.example.backend.entity.property.UserRole;
import com.example.backend.entity.property.VolunteerRewardStatus;
import com.example.backend.entity.property.VolunteerRewardType;
import com.example.backend.facade.VolunteerFacade;
import com.example.backend.mapper.*;
import com.example.backend.util.CustomUserDetails;
import com.example.backend.util.ServiceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class VolunteerServiceUnitTest {

    private VolunteerRewardMapper volunteerRewardMapper;
    private VolunteerFacade volunteerFacade;
    private VolunteerService service;

    @BeforeEach
    void setUp() {
        volunteerRewardMapper = mock(VolunteerRewardMapper.class);
        volunteerFacade = mock(VolunteerFacade.class);
        service = new VolunteerService(
                mock(VolunteerApplicationMapper.class),
                mock(VolunteerProfileMapper.class),
                mock(VolunteerShiftMapper.class),
                mock(VolunteerShiftStatusRecordMapper.class),
                mock(VolunteerServiceRecordMapper.class),
                volunteerRewardMapper,
                mock(VolunteerTaskMapper.class),
                mock(LocationMapper.class),
                volunteerFacade,
                mock(InformationService.class));
        ReflectionTestUtils.setField(service, "baseMapper", mock(VolunteerRecruitmentMapper.class));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void workerCanIssuePendingReward() {
        User worker = user(3L, "worker", UserRole.WORKER.getMask());
        login(worker);
        VolunteerReward reward = reward(40L, VolunteerRewardStatus.PENDING);
        when(volunteerRewardMapper.requireById(40L)).thenReturn(reward);
        when(volunteerRewardMapper.updateById(any(VolunteerReward.class))).thenReturn(1);
        VolunteerRewardResponse expected = VolunteerRewardResponse.create(reward, user(20L, "volunteer", UserRole.VOLUNTEER.getMask()), worker);
        when(volunteerFacade.buildRewardResponse(reward)).thenReturn(expected);

        VolunteerRewardResponse response = service.issueReward(40L);

        assertSame(expected, response);
        assertEquals(VolunteerRewardStatus.ISSUED, reward.getStatus());
        assertEquals(3L, reward.getIssuerId());
        assertNotNull(reward.getIssueTime());
        assertNotNull(reward.getUpdateTime());
        verify(volunteerRewardMapper).updateById(reward);
    }

    @Test
    void normalUserCannotIssueReward() {
        login(user(4L, "normal", UserRole.NORMAL.getMask()));

        ServiceException ex = assertThrows(ServiceException.class, () -> service.issueReward(40L));

        assertEquals(ServiceException.E_AUTH, ex.getCode());
        verify(volunteerRewardMapper, never()).requireById(anyLong());
    }

    @Test
    void issuedRewardCannotBeIssuedAgain() {
        login(user(3L, "worker", UserRole.WORKER.getMask()));
        VolunteerReward reward = reward(40L, VolunteerRewardStatus.ISSUED);
        when(volunteerRewardMapper.requireById(40L)).thenReturn(reward);

        ServiceException ex = assertThrows(ServiceException.class, () -> service.issueReward(40L));

        assertEquals(ServiceException.E_INVALIDATE, ex.getCode());
        assertEquals("exception.invalidate.volunteer.reward.issue_status_invalid", ex.getMessage());
        verify(volunteerRewardMapper, never()).updateById(any(VolunteerReward.class));
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

    private static VolunteerReward reward(Long id, VolunteerRewardStatus status) {
        return new VolunteerReward(id,
                20L,
                new Date(),
                new Date(),
                5,
                BigDecimal.TEN,
                VolunteerRewardType.CERTIFICATE,
                "热心志愿者",
                "服务表现优秀",
                status,
                null,
                null,
                null,
                new Date(),
                new Date());
    }
}
