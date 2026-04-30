package com.example.backend.service;

import com.example.backend.dto.DonationStatusUpdateRequest;
import com.example.backend.entity.Donation;
import com.example.backend.entity.DonationStatusUpdateRecord;
import com.example.backend.entity.User;
import com.example.backend.entity.property.DeliveryType;
import com.example.backend.entity.property.DonationStatus;
import com.example.backend.entity.property.UserRole;
import com.example.backend.facade.ItemDonationFacade;
import com.example.backend.mapper.*;
import com.example.backend.util.CustomUserDetails;
import com.example.backend.util.MPLambdaUpdate;
import com.example.backend.util.ServiceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ItemDonationServiceUnitTest {

    private DonationMapper donationMapper;
    private DonationStatusUpdateMapper donationStatusUpdateMapper;
    private MPLambdaUpdate<Donation> update;
    private ItemDonationService service;

    @BeforeEach
    void setUp() {
        donationMapper = mock(DonationMapper.class);
        donationStatusUpdateMapper = mock(DonationStatusUpdateMapper.class);
        update = mock();
        service = new ItemDonationService(
                donationMapper,
                mock(DonationItemMapper.class),
                mock(DonationFileMapper.class),
                donationStatusUpdateMapper,
                mock(CategoryMapper.class),
                mock(StockMapper.class),
                mock(StockRecordMapper.class),
                mock(SubscribeMapper.class),
                mock(ItemDonationFacade.class));
        ReflectionTestUtils.setField(service, "baseMapper", mock(ItemMapper.class));
        service.setObjects(mock(), mock(ApplicationEventPublisher.class), mock(), mock(), mock());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void donationOwnerCanCloseBackingDonation() {
        login(user(10L, "donor", UserRole.NORMAL.getMask()));
        Donation donation = donation(33L, 10L, DonationStatus.BACKING);
        DonationStatusUpdateRequest request = request(DonationStatus.CLOSED);
        when(donationMapper.requireById(33L)).thenReturn(donation);
        when(donationMapper.updateStatus(33L, DonationStatus.CLOSED)).thenReturn(update);

        service.updateDonationStatus(33L, request);

        verify(donationStatusUpdateMapper).insert(argThat((DonationStatusUpdateRecord record) ->
                record.getDonationId().equals(33L)
                        && record.getUserId().equals(10L)
                        && record.getOldStatus() == DonationStatus.BACKING
                        && record.getNewStatus() == DonationStatus.CLOSED));
        verify(update).update();
    }

    @Test
    void normalOwnerCannotMoveCreatedDonationToPending() {
        login(user(10L, "donor", UserRole.NORMAL.getMask()));
        when(donationMapper.requireById(33L)).thenReturn(donation(33L, 10L, DonationStatus.CREATED));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.updateDonationStatus(33L, request(DonationStatus.PENDING)));

        assertEquals(ServiceException.E_AUTH, ex.getCode());
        verify(donationStatusUpdateMapper, never()).insert(any(DonationStatusUpdateRecord.class));
    }

    @Test
    void workerCannotApplyInvalidDonationStatusTransition() {
        login(user(11L, "worker", UserRole.WORKER.getMask()));
        when(donationMapper.requireById(33L)).thenReturn(donation(33L, 10L, DonationStatus.STOCKED));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.updateDonationStatus(33L, request(DonationStatus.PENDING)));

        assertEquals(ServiceException.E_INVALIDATE, ex.getCode());
        assertEquals("exception.invalidate.donation_status", ex.getMessage());
        verify(donationStatusUpdateMapper, never()).insert(any(DonationStatusUpdateRecord.class));
    }

    private static DonationStatusUpdateRequest request(DonationStatus status) {
        DonationStatusUpdateRequest request = new DonationStatusUpdateRequest();
        request.setStatus(status.name());
        request.setReason("测试状态流转");
        return request;
    }

    private static Donation donation(Long id, Long userId, DonationStatus status) {
        return new Donation(id,
                userId,
                DeliveryType.FACE,
                null,
                null,
                "捐赠猫粮",
                status,
                new Date(),
                new Date());
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
}
