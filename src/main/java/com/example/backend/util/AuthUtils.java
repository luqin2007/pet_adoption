package com.example.backend.util;

import com.example.backend.entity.IUserRole;
import com.example.backend.entity.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static com.example.backend.util.C.*;

public class AuthUtils {

    /**
     * 获取当前登录用户
     */
    public static Optional<User> getLoginUserOpt() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(auth -> (CustomUserDetails) auth.getPrincipal())
                .map(CustomUserDetails::getUser);
    }

    /**
     * 获取当前登录用户
     */
    public static User getLoginUser() {
        return getLoginUserOpt().orElseThrow(() -> ServiceException.auth("请先登录"));
    }

    public static Collection<SimpleGrantedAuthority> createAuthorities(IUserRole user) {
        Set<String> authorities = new HashSet<>();
        authorities.add(ROLE_NORMAL);
        if (user.isAdmin()) {
            // 超级管理员
            authorities.add(ROLE_VOLUNTEER);
            authorities.add(ROLE_WORKER);
            authorities.add(ROLE_DONOR);
            authorities.add(ROLE_VETERINARIAN);
            authorities.add(ROLE_ADMIN);
        } else {
            if (user.isWorker()) {
                // 救助站工作人员
                authorities.add(ROLE_VOLUNTEER);
                authorities.add(ROLE_WORKER);
            } else if (user.isVolunteer()) {
                // 志愿者
                authorities.add(ROLE_VOLUNTEER);
            }

            if (user.isDonor())
                // 捐赠者
                authorities.add(ROLE_DONOR);

            if (user.isVeterinarian())
                // 兽医
                authorities.add(ROLE_VETERINARIAN);
        }
        return authorities.stream().map(SimpleGrantedAuthority::new).toList();
    }

    public static Integer getRoleCode(Integer role) {
        if (role == null) return 0;

        if ((role & MASK_ADMIN) == MASK_ADMIN)
            // 超级管理员
            return 0x1F;

        int i = 0;
        if ((role & MASK_VETERINARIAN) == MASK_VETERINARIAN)
            // 兽医
            i |= MASK_VETERINARIAN;

        if ((role & MASK_DONOR) == MASK_DONOR)
            // 捐赠者
            i |= MASK_DONOR;

        if ((role & MASK_WORKER) == MASK_WORKER) {
            // 救助站工作人员
            return i | MASK_WORKER | MASK_VOLUNTEER;
        } else if ((role & MASK_VOLUNTEER) == MASK_VOLUNTEER) {
            // 志愿者
            return i | MASK_VOLUNTEER;
        } else {
            return i;
        }
    }
}
