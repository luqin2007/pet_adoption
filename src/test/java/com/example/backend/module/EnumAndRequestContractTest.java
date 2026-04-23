package com.example.backend.module;

import com.example.backend.dto.IdsRequest;
import com.example.backend.entity.property.*;
import com.example.backend.util.ServiceException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

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
