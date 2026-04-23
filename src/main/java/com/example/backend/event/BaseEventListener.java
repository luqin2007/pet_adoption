package com.example.backend.event;

import com.example.backend.entity.User;
import com.example.backend.entity.property.NoticeSource;
import com.example.backend.entity.property.UserRole;
import com.example.backend.mapper.UserMapper;
import com.example.backend.util.LangHelper;
import com.example.backend.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unchecked")
public class BaseEventListener {

    private UserMapper userMapper;
    private ApplicationEventPublisher eventPublisher;
    protected LangHelper langHelper;

    public void notify(Long userId, INotifyEvent<?> event, Object... contentArgs) {
        notify(List.of(userId), event, contentArgs);
    }

    public void notify(Collection<Long> userIds, INotifyEvent<?> event, Object... contentArgs) {
        notify(null, userIds, event.getSource(),
                event.buildNotifyTitle(langHelper),
                event.buildNotifyContent(langHelper, contentArgs));
    }

    public void notify(User exclude, Collection<Long> userIds, INotifyEvent<?> event, Object... contentArgs) {
        notify(exclude == null ? null : exclude.getId(), userIds, event.getSource(),
                event.buildNotifyTitle(langHelper),
                event.buildNotifyContent(langHelper, contentArgs));
    }

    public Set<Long> notify(Long exceptedId, Collection<Long> userIds, NoticeSource source, String title, String content) {
        Set<Long> ids = sanitizeIds(userIds, exceptedId);
        if (ids.isEmpty()) return ids;
        eventPublisher.publishEvent(new NotifyEvent(ids, source, title, content));
        return ids;
    }

    public Set<Long> notifyWorkers(Long excludeId, INotifyEvent<?> event, Object... contentArgs) {
        Set<Long> userIds = userMapper.queryByRole(UserRole.WORKER.getMask())
                .list(User::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return notify(excludeId, userIds, event.getSource(),
                event.buildNotifyTitle(langHelper),
                event.buildNotifyContent(langHelper, contentArgs));
    }

    /**
     * 发送状态变更通知
     *
     * @return 是否是审核人回复
     */
    protected boolean notifyReview(IReviewEvent<?> event, Object... contentArgs) {
        User user = event.user();
        if (user.is(event.applicantId())) { // 申请人回复
            if (event.reviewerId() != null) { // 有审核人
                notify(event.reviewerId(), event, contentArgs);
            } else { // 无审核人
                notifyWorkers(event.applicantId(), event, contentArgs);
            }
            return false;
        } else { // 审核人回复
            notify(event.applicantId(), event, contentArgs);
            return true;
        }
    }

    public void sendEmail(Collection<Long> userIds, INotifyEvent<?> event, Object... contentArgs) {
        sendEmail(userIds,
                event.buildMailTitle(langHelper),
                event.buildMailContent(langHelper, contentArgs));
    }

    public void sendEmail(Long userId, INotifyEvent<?> event, Object... contentArgs) {
        sendEmail(List.of(userId),
                event.buildMailTitle(langHelper),
                event.buildMailContent(langHelper, contentArgs));
    }

    public void sendEmail(Collection<Long> userIds, String title, String content) {
        Set<String> emails = getEmails(userIds);
        eventPublisher.publishEvent(new MailSendEvent(emails, title, content));
    }

    private Set<Long> sanitizeIds(Collection<Long> userIds, Long exceptedId) {
        Stream<Long> idStream = userIds.stream().filter(id -> id != null && id > 0);
        if (exceptedId != null) {
            idStream = idStream.filter(id -> !id.equals(exceptedId));
        }
        return idStream.collect(Collectors.toSet());
    }

    private Set<String> getEmails(Collection<Long> userIds) {
        Set<Long> userIdSet = sanitizeIds(userIds, null);
        if (userIdSet.isEmpty()) return Set.of();
        if (userIdSet.size() == 1) {
            Long userId = userIdSet.iterator().next();
            String email = userMapper.requireById(userId, User::getEmail).getEmail();
            return StringUtils.hasText(email) ? Set.of(email) : Set.of();
        }
        return userMapper.selectList(userIdSet, User::getEmail).stream()
                .map(User::getEmail)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
    }

    @Autowired
    public void setObjects(UserMapper userMapper,
                           LangHelper langHelper,
                           ApplicationEventPublisher eventPublisher) {
        this.userMapper = userMapper;
        this.langHelper = langHelper;
        this.eventPublisher = eventPublisher;
    }
}
