package com.example.backend.event;

import java.util.Set;

/**
 * 发送邮件
 */
public record MailSendEvent(Set<String> emails, String title, String content) {
}
