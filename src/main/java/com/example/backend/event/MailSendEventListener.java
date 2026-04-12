package com.example.backend.event;

import com.example.backend.util.ServiceException;
import com.example.backend.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class MailSendEventListener {

    @Value("${spring.mail.username}")
    private String mailFrom;

    private final MailSender mailSender;

    @Async
    @EventListener(MailSendEvent.class)
    public void onMailSend(MailSendEvent event) {
        // 检查邮件
        String[] addresses = Set.of(event.addresses())
                .stream()
                .filter(StringUtils::hasText)
                .filter(address -> address.contains("@"))
                .toArray(String[]::new);
        if (addresses.length == 0) return;
        // 发送邮件
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(mailFrom);
        mail.setTo(addresses);
        mail.setSubject(event.title());
        mail.setText(event.content());
        try {
            mailSender.send(mail);
        } catch (Exception e) {
            throw ServiceException.system("exception.system.mail_send_failed", e);
        }
    }
}
