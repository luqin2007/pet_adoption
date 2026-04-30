package com.example.backend.util;

import com.example.backend.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.ObjectUtils;

import java.util.Objects;
import java.util.Optional;

/**
 * 校验相关接口方法
 */
public interface IValidates {

    /**
     * 获取当前登录用户
     */
    default User requireLoginUser() {
        return getLoginUser().orElseThrow(() -> ServiceException.auth("exception.auth.required"));
    }

    /**
     * 获取当前登录用户
     */
    default Optional<User> getLoginUser() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getPrincipal)
                .filter(user -> user instanceof CustomUserDetails)
                .map(user -> (CustomUserDetails) user)
                .map(CustomUserDetails::getUser);
    }

    /**
     * 权限校验
     */
    default void requirePermission(boolean permission) {
        if (!permission) throw ServiceException.auth("exception.auth.denied");
    }

    /**
     * 确保对象存在
     */
    default void requireExist(Object obj, String message) {
        if (ObjectUtils.isEmpty(obj)) throw ServiceException.notFound(message);
    }

    /**
     * 确保对象相同
     */
    default void requireEqual(Object obj1, Object obj2, String message) {
        if (!Objects.equals(obj1, obj2)) {
            throw ServiceException.invalidate(message);
        }
    }

    /**
     * 确保对象相同
     */
    default void requireEqual(Enum<?> obj1, Object obj2, String message) {
        if (obj1 != obj2) {
            throw ServiceException.invalidate(message);
        }
    }

    /**
     * 确保值为 true
     */
    default void require(boolean value, String message) {
        if (!value) {
            throw ServiceException.invalidate(message);
        }
    }
}
