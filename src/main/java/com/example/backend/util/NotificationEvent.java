package com.example.backend.util;

import java.util.Set;

/**
 * 发送通知事件
 * @param title 标题
 * @param content 内容
 * @param target 跳转目标，仅通知有效
 * @param emails 发送到特定邮箱
 * @param users 发送到特定用户
 * @param roles 发送到特定角色
 * @param sendEmailToUsers 是否为 users/roles 筛选出的用户发送邮件
 */
public record NotificationEvent(
        String title,
        String content,
        JumpTarget target,
        Set<String> emails,
        Set<Long> users,
        Set<String> roles,
        boolean sendEmailToUsers
) {
    public static NotificationEvent mails(String title, String content, Set<String> emails) {
        return new NotificationEvent(title, content, JumpTarget.EMPTY, emails, Set.of(), Set.of(), false);
    }

    public enum JumpTarget {
        EMPTY
    }
}
