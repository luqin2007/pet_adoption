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
    @EventListener(NotificationEvent.Mail.class)
    public void onNotification(NotificationEvent.Mail event) {
        Set<String> emails = new HashSet<>(event.getAddresses());
        emails.addAll(userService.getMailsBatchByRoles(event.getRoles()));
        // 发送邮件
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(mailFrom);
        mail.setTo(emails.toArray(String[]::new));
        mail.setSubject(event.getTitle());
        mail.setText(event.getContent());
        try {
            mailSender.send(mail);
        } catch (Exception e) {
            throw ServiceException.system("邮件发送失败: " + e.getMessage(), e);
        }
    }

    @Async
    @EventListener(NotificationEvent.System.class)
    public void onNotification(NotificationEvent.System event) {
        Set<Long> ids = new HashSet<>(event.getUsers());
        ids.addAll(userService.getIdsBatchByRoles(event.getRoles()));
        // TODO 发送通知
    }
}
