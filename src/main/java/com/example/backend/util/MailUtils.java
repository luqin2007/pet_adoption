package com.example.backend.util;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MailUtils {

    @Value("${spring.mail.username}")
    private String from;

    private final MailSender mailSender;

    public void send(String receiver, String subject, String content) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(from);
        mail.setTo(receiver);
        mail.setSubject(subject);
        mail.setText(content);
        try {
            mailSender.send(mail);
        } catch (Exception e) {
            throw new ServiceException("邮件发送失败: " + e.getMessage(), e);
        }
    }
}
