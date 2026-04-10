package com.example.backend.event;

/**
 * 发送邮件
 */
public record MailSendEvent(String title, String content, String... addresses) {
}
