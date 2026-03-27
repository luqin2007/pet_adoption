package com.example.backend.util;

import com.example.backend.entity.property.UserRole;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

/**
 * 发送通知事件
 */
@Data
public sealed abstract class NotificationEvent permits NotificationEvent.Mail, NotificationEvent.System {

    private String title;

    private String content;

    /**
     * 发送邮件
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static final class Mail extends NotificationEvent {

        private Set<String> addresses = Set.of();

        private Set<UserRole> roles = Set.of();
    }

    /**
     * 发送站内通知
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static final class System extends NotificationEvent {

        private Set<UserRole> roles = Set.of();

        private Set<Long> users = Set.of();

        private String jumpTo;
    }

    /**
     * 创建向指定邮箱发送邮件的事件
     *
     * @param title   标题
     * @param content 内容
     * @param address 邮件地址
     */
    public static NotificationEvent.Mail mail(String title, String content, String address) {
        NotificationEvent.Mail mail = new NotificationEvent.Mail();
        mail.setTitle(title);
        mail.setContent(content);
        mail.setAddresses(Set.of(address));
        return mail;
    }

    /**
     * 创建向指定邮箱群发邮件的事件
     *
     * @param title     标题
     * @param content   内容
     * @param addresses 邮件地址
     */
    public static NotificationEvent.Mail mail(String title, String content, Set<String> addresses) {
        NotificationEvent.Mail mail = new NotificationEvent.Mail();
        mail.setTitle(title);
        mail.setContent(content);
        mail.setAddresses(addresses);
        return mail;
    }

    /**
     * 创建向指定角色用户群发邮件的事件
     *
     * @param title   标题
     * @param content 内容
     * @param role    用户角色
     */
    public static NotificationEvent.Mail mail(String title, String content, UserRole role) {
        NotificationEvent.Mail mail = new NotificationEvent.Mail();
        mail.setTitle(title);
        mail.setContent(content);
        mail.setRoles(Set.of(role));
        return mail;
    }

    /**
     * 创建向指定角色用户群发站内通知的事件
     *
     * @param title   标题
     * @param content 内容
     * @param role    用户角色
     */
    public static NotificationEvent.System system(String title, String content, String jumpTo, UserRole role) {
        NotificationEvent.System system = new NotificationEvent.System();
        system.setTitle(title);
        system.setContent(content);
        system.setJumpTo(jumpTo);
        system.setRoles(Set.of(role));
        return system;
    }

    /**
     * 创建向指定用户群发站内通知的事件
     *
     * @param title   标题
     * @param content 内容
     * @param users   用户
     */
    public static NotificationEvent.System system(String title, String content, String jumpTo, Set<Long> users) {
        NotificationEvent.System system = new NotificationEvent.System();
        system.setTitle(title);
        system.setContent(content);
        system.setJumpTo(jumpTo);
        system.setUsers(users);
        return system;
    }
}
