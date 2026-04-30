package com.example.backend.module;

import com.example.backend.dto.IdsRequest;
import com.example.backend.entity.property.*;
import com.example.backend.util.ServiceException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class EnumAndRequestContractTest {

    @Test
    void rescueTaskStatusAllowsOnlyDocumentedForwardTransitions() {
        assertTrue(RescueTaskStatus.APPROVED.canChangeFrom(RescueTaskStatus.CREATED));
        assertTrue(RescueTaskStatus.PROCESSING.canChangeFrom(RescueTaskStatus.APPROVED));
        assertTrue(RescueTaskStatus.COMPLETED.canChangeFrom(RescueTaskStatus.APPROVED));
        assertTrue(RescueTaskStatus.COMPLETED.canChangeFrom(RescueTaskStatus.PROCESSING));
        assertTrue(RescueTaskStatus.DISCARDED.canChangeFrom(RescueTaskStatus.CREATED));
        assertFalse(RescueTaskStatus.CREATED.canChangeFrom(RescueTaskStatus.APPROVED));
        assertFalse(RescueTaskStatus.PROCESSING.canChangeFrom(RescueTaskStatus.CREATED));
    }

    @Test
    void donationStatusAllowsOnlyDocumentedForwardTransitions() {
        assertTrue(DonationStatus.PENDING.canChangeFrom(DonationStatus.CREATED));
        assertTrue(DonationStatus.TRANSFERRING.canChangeFrom(DonationStatus.CREATED));
        assertTrue(DonationStatus.TRANSFERRING.canChangeFrom(DonationStatus.PENDING));
        assertTrue(DonationStatus.RECEIVED.canChangeFrom(DonationStatus.TRANSFERRING));
        assertTrue(DonationStatus.STOCKED.canChangeFrom(DonationStatus.RECEIVED));
        assertTrue(DonationStatus.REFUSED.canChangeFrom(DonationStatus.PENDING));
        assertTrue(DonationStatus.BACKING.canChangeFrom(DonationStatus.REFUSED));
        assertTrue(DonationStatus.CLOSED.canChangeFrom(DonationStatus.BACKING));
        assertTrue(DonationStatus.CANCELED.canChangeFrom(DonationStatus.CREATED));
        assertFalse(DonationStatus.CREATED.canChangeFrom(DonationStatus.PENDING));
        assertFalse(DonationStatus.STOCKED.canChangeFrom(DonationStatus.PENDING));
    }

    @Test
    void volunteerApplicationStatusAllowsOnlyDocumentedForwardTransitions() {
        assertTrue(VolunteerApplicationStatus.UNDER_REVIEW.canSwitchFrom(VolunteerApplicationStatus.SUBMITTED));
        assertTrue(VolunteerApplicationStatus.APPROVED.canSwitchFrom(VolunteerApplicationStatus.SUBMITTED));
        assertTrue(VolunteerApplicationStatus.APPROVED.canSwitchFrom(VolunteerApplicationStatus.UNDER_REVIEW));
        assertTrue(VolunteerApplicationStatus.REJECTED.canSwitchFrom(VolunteerApplicationStatus.UNDER_REVIEW));
        assertTrue(VolunteerApplicationStatus.CANCELED.canSwitchFrom(VolunteerApplicationStatus.SUBMITTED));
        assertFalse(VolunteerApplicationStatus.SUBMITTED.canSwitchFrom(VolunteerApplicationStatus.UNDER_REVIEW));
        assertFalse(VolunteerApplicationStatus.UNDER_REVIEW.canSwitchFrom(VolunteerApplicationStatus.APPROVED));
    }

    @Test
    void userRoleExpandsImplicitPermissionsAndRezipNormalizesMask() {
        assertEquals(Set.of(UserRole.NORMAL),
                UserRole.getRoles(UserRole.NORMAL.getMask()).collect(Collectors.toSet()));
        assertEquals(Set.of(UserRole.NORMAL, UserRole.VOLUNTEER, UserRole.WORKER),
                UserRole.getRoles(UserRole.WORKER.getMask()).collect(Collectors.toSet()));
        assertEquals(Set.of(UserRole.NORMAL, UserRole.VOLUNTEER, UserRole.WORKER, UserRole.DONOR, UserRole.DOCTOR, UserRole.ADMIN),
                UserRole.getRoles(UserRole.ADMIN.getMask()).collect(Collectors.toSet()));

        assertEquals(UserRole.WORKER.getMask() | UserRole.VOLUNTEER.getMask(),
                UserRole.rezip(UserRole.WORKER.getMask()));
        assertEquals(UserRole.MAX_ROLE, UserRole.rezip(UserRole.ADMIN.getMask()));
        assertTrue(UserRole.NORMAL.match(0));
        assertTrue(UserRole.VOLUNTEER.match(UserRole.WORKER.getMask()));
        assertTrue(UserRole.DOCTOR.match(UserRole.ADMIN.getMask()));
        assertFalse(UserRole.ADMIN.match(UserRole.WORKER.getMask()));
    }

    @Test
    void publicationStatusTransitionsAreConstrained() {
        assertTrue(ArticleStatus.PUBLISHED.canChangeFrom(ArticleStatus.DRAFT));
        assertTrue(ArticleStatus.OFFLINE.canChangeFrom(ArticleStatus.PUBLISHED));
        assertTrue(ArticleStatus.DRAFT.canChangeFrom(ArticleStatus.PUBLISHED));
        assertFalse(ArticleStatus.PUBLISHED.canChangeFrom(ArticleStatus.OFFLINE));
        assertFalse(ArticleStatus.OFFLINE.canChangeFrom(ArticleStatus.DRAFT));
    }

    @Test
    void enumParsersAreCaseInsensitiveAndRejectUnknownValues() {
        assertEquals(ParentType.LOST_PET, ParentType.get("lost_pet"));
        assertEquals(ArticleType.KNOWLEDGE, ArticleType.get("knowledge"));
        assertEquals(NoticeSource.RESCUE_TASK, NoticeSource.get("rescue_task"));
        assertEquals(VolunteerRewardType.CERTIFICATE, VolunteerRewardType.get("certificate"));

        ServiceException ex = assertThrows(ServiceException.class, () -> RescueTaskType.get("missing"));
        assertEquals(ServiceException.E_INVALIDATE, ex.getCode());
        assertEquals("exception.invalidate.rescue_task_type", ex.getMessage());
    }

    @Test
    void idsRequestRemovesNullsAndDuplicates() {
        IdsRequest request = new IdsRequest();
        request.setIds(List.of(3L, 2L, 3L, 1L));

        assertEquals(Set.of(1L, 2L, 3L), request.idSet());
    }
}
