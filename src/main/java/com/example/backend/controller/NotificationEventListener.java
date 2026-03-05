package com.example.backend.controller;

import com.example.backend.service.UserService;
import com.example.backend.util.NotificationEvent;
import com.example.backend.util.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    @Value("${spring.mail.username}")
    private String mailFrom;

    private final UserService userService;
    private final MailSender mailSender;

    @Async
    @EventListener(NotificationEvent.class)
    public void onNotification(NotificationEvent event) {
        Set<Long> ids = new HashSet<>(event.users());
        if (!event.roles().isEmpty()) {
            ids.addAll(userService.getIdsBatchByRoles(event.roles()));
        }
        sendAppNotification(ids.toArray(Long[]::new), event);

        Set<String> emails = new HashSet<>(event.emails());
        if (event.sendEmailToUsers() && !event.users().isEmpty()) {
            emails.addAll(userService.getMailsBatchByIds(event.users()));
        }
        sendEmail(emails.toArray(String[]::new), event.title(), event.content());
    }

    /**
     * 发送邮件
     */
    private void sendEmail(String[] to, String title, String content) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(mailFrom);
        mail.setTo(to);
        mail.setSubject(title);
        mail.setText(content);
        try {
            mailSender.send(mail);
        } catch (Exception e) {
            throw new ServiceException("邮件发送失败: " + e.getMessage(), e);
        }
    }

    private void sendAppNotification(Long[] ids, NotificationEvent event) {
        // TODO 发送通知
    }
}
