package com.example.backend.service;

import com.example.backend.entity.Adopt;
import com.example.backend.entity.User;
import com.example.backend.entity.property.AdoptBreadingStatus;
import com.example.backend.entity.property.UserRole;
import com.example.backend.facade.AdoptBreadingFacade;
import com.example.backend.mapper.*;
import com.example.backend.util.CustomUserDetails;
import com.example.backend.util.ServiceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class AdoptBreadingServiceUnitTest {

    private AdoptMapper adoptMapper;
    private AdoptBreadingService service;

    @BeforeEach
    void setUp() {
        adoptMapper = mock(AdoptMapper.class);
        service = new AdoptBreadingService(
                mock(BreadingMapper.class),
                mock(AgreementMapper.class),
                mock(AgreementFileMapper.class),
                mock(AgreementUpdateRecordMapper.class),
                mock(FollowTaskMapper.class),
                mock(FollowRecordMapper.class),
                mock(AdoptBreadingFacade.class),
                mock(LocationMapper.class),
                mock(InformationService.class));
        ReflectionTestUtils.setField(service, "baseMapper", adoptMapper);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void workerCannotSetAdoptStatusToAgreementSignedDirectly() {
        login(user(1L, "worker", UserRole.WORKER.getMask()));
        Adopt adopt = adopt(10L, AdoptBreadingStatus.PASS);
        when(adoptMapper.requireById(10L)).thenReturn(adopt);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.updateAdoptStatus(10L, AdoptBreadingStatus.AGREEMENT_SIGNED.name()));

        assertEquals(ServiceException.E_INVALIDATE, ex.getCode());
        assertEquals("exception.invalidate.adopt.status_abnormal", ex.getMessage());
        verify(adoptMapper, never()).updateById(any(Adopt.class));
    }

    @Test
    void normalUserCannotChangeAdoptStatus() {
        login(user(2L, "normal", UserRole.NORMAL.getMask()));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.updateAdoptStatus(10L, AdoptBreadingStatus.PASS.name()));

        assertEquals(ServiceException.E_AUTH, ex.getCode());
        verify(adoptMapper, never()).requireById(anyLong());
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

    private static Adopt adopt(Long id, AdoptBreadingStatus status) {
        return new Adopt(id,
                20L,
                30L,
                "13800000000",
                null,
                status,
                "有稳定住所",
                null,
                null,
                null,
                new Date(),
                new Date());
    }
}
