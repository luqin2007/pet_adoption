package com.example.backend.util;

import com.example.backend.entity.property.UserRole;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collection;
import java.util.Set;

/**
 * 发送通知事件
 */
@Data
public sealed abstract class NotificationEvent permits NotificationEvent.Mail, NotificationEvent.System {

    private String title;

    private String content;

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static final class Mail extends NotificationEvent {

        private Set<String> addresses = Set.of();

        private Set<UserRole> roles = Set.of();
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static final class System extends NotificationEvent {

        private Set<UserRole> roles = Set.of();

        private Set<Long> users = Set.of();

        private String jumpTo;
    }

    public static NotificationEvent.Mail mail(String title, String content, String address) {
        NotificationEvent.Mail mail = new NotificationEvent.Mail();
        mail.setTitle(title);
        mail.setContent(content);
        mail.setAddresses(Set.of(address));
        return mail;
    }

    public static NotificationEvent.Mail mail(String title, String content, Set<String> addresses) {
        NotificationEvent.Mail mail = new NotificationEvent.Mail();
        mail.setTitle(title);
        mail.setContent(content);
        mail.setAddresses(addresses);
        return mail;
    }

    public static NotificationEvent.Mail mailToRoles(String title, String content, UserRole role) {
        NotificationEvent.Mail mail = new NotificationEvent.Mail();
        mail.setTitle(title);
        mail.setContent(content);
        mail.setRoles(Set.of(role));
        return mail;
    }

    public static NotificationEvent.System system(String title, String content, String jumpTo, UserRole... roles) {
        NotificationEvent.System system = new NotificationEvent.System();
        system.setTitle(title);
        system.setContent(content);
        system.setJumpTo(jumpTo);
        system.setRoles(Set.of(roles));
        return system;
    }

    public static NotificationEvent.System system(String title, String content, String jumpTo, Set<Long> users) {
        NotificationEvent.System system = new NotificationEvent.System();
        system.setTitle(title);
        system.setContent(content);
        system.setJumpTo(jumpTo);
        system.setUsers(users);
        return system;
    }
}
