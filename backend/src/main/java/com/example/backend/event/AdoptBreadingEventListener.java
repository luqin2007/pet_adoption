package com.example.backend.event;

import com.example.backend.entity.Adopt;
import com.example.backend.entity.FollowTask;
import com.example.backend.entity.Pet;
import com.example.backend.mapper.AdoptMapper;
import com.example.backend.mapper.BreadingMapper;
import com.example.backend.mapper.FollowTaskMapper;
import com.example.backend.mapper.PetMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Set;

// TODO 在通知中引导到领养详情和领养审核页
@SuppressWarnings("unchecked")
@Component
@RequiredArgsConstructor
public class AdoptBreadingEventListener extends BaseEventListener {

    private final AdoptMapper adoptMapper;
    private final BreadingMapper breadingMapper;
    private final PetMapper petMapper;
    private final FollowTaskMapper followTaskMapper;

    @TransactionalEventListener
    public void onAdoptAdd(AdoptAddEvent event) {
        String name = petMapper.requireById(event.data().getPetId(), Pet::getName).getName("<未命名>");
        notifyWorkers(event.data().getApplicantId(), event, name);
    }

    @TransactionalEventListener
    public void onAdoptStatus(AdoptStatusEvent event) {
        Adopt adopt = event.data();
        String name = petMapper.requireById(adopt.getPetId(), Pet::getName).getName("");
        boolean replyToUser = notifyReview(event, name);
        if (replyToUser) {
            sendEmail(adopt.getApplicantId(), event, name);
        }
    }

    @TransactionalEventListener
    public void onBreadingAdd(BreadingAddEvent event) {
        notifyWorkers(event.data().getApplicantId(), event);
    }

    @TransactionalEventListener
    public void onBreadingStatus(BreadingStatusEvent event) {
        boolean replyToUser = notifyReview(event);
        if (replyToUser) {
            sendEmail(event.data().getApplicantId(), event);
        }
    }

    @TransactionalEventListener
    public void onAgreementAdd(AgreementAddEvent event) {
        Long userId = event.data().getApplicantId();
        notify(userId, event);
        sendEmail(userId, event);
    }

    @TransactionalEventListener
    public void onAgreementUpdate(AgreementUpdateEvent event) {
        Long applicantId = event.data().getApplicantId();
        if (event.user().is(applicantId)) { // 申请人回复
            Long reviewerId = event.data().getReviewerId();
            if (reviewerId == null) {
                Set<Long> userIds = notifyWorkers(null, event);
                sendEmail(userIds, event);
            } else {
                notify(reviewerId, event);
                sendEmail(reviewerId, event);
            }
        } else {
            notify(applicantId, event);
            sendEmail(applicantId, event);
        }
    }

    @TransactionalEventListener
    public void onFollowTaskAdd(FollowTaskAddEvent event) {
        FollowTask task = event.data();
        // 通知志愿者
        notify(task.getVolunteerId(), event);
        sendEmail(task.getVolunteerId(), event);
        // 通知领养人
        Adopt adopt = adoptMapper.requireById(task.getAdoptId(), Adopt::getApplicantId);
        notify(adopt.getApplicantId(), event);
        sendEmail(adopt.getApplicantId(), event);
        // 更新状态
        followTaskMapper.updateNotified(task.getId()).update();
    }

    @TransactionalEventListener
    public void onFollowTaskUpdate(FollowTaskUpdateEvent event) {
        FollowTask task = event.data();
        Adopt adopt = adoptMapper.requireById(task.getAdoptId(), Adopt::getApplicantId);
        Set<Long> notifyIds = Set.of(adopt.getApplicantId(), task.getVolunteerId(), task.getWorkerId());
        notify(event.user(), notifyIds, event);
        // 邮件通知领养人
        if (!event.user().is(adopt.getApplicantId()))
            sendEmail(task.getVolunteerId(), event);
    }

    @TransactionalEventListener
    public void onFollowRecord(FollowRecordEvent event) {
        FollowTask task = followTaskMapper.requireById(event.data().getTaskId(),
                FollowTask::getId, FollowTask::getAdoptId, FollowTask::getWorkerId, FollowTask::getVolunteerId);
        Adopt adopt = adoptMapper.requireById(task.getAdoptId(),
                Adopt::getId, Adopt::getApplicantId);
        notify(event.user(), List.of(task.getWorkerId(), adopt.getApplicantId()), event);
    }
}
