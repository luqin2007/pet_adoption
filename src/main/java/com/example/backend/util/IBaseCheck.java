package com.example.backend.util;

import com.example.backend.entity.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.ObjectUtils;

import java.util.Objects;
import java.util.Optional;

public interface IBaseCheck {

    /**
     * 获取当前登录用户
     */
    default User getLoginUser() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(auth -> (CustomUserDetails) auth.getPrincipal())
                .map(CustomUserDetails::getUser)
                .orElseThrow(() -> ServiceException.auth("请先登录"));
    }

    /**
     * 权限校验
     */
    default void requirePermission(boolean permission) {
        if (!permission) throw ServiceException.auth("权限不足");
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
        if (obj1 != null && !obj1.equals(obj2)) {
            throw ServiceException.invalidate(message);
        }
    }

    /**
     * 确保对象不同
     */
    default void requireNotEqual(Object obj1, Object obj2, String message) {
        if (!Objects.equals(obj1, obj2)) {
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
