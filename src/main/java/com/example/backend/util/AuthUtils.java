package com.example.backend.util;

import com.example.backend.entity.IUserRole;
import com.example.backend.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;

@Component
public class AuthUtils {

    // 爱心人士
    public static final String ROLE_NORMAL = "ROLE_NORMAL";
    // 志愿者
    public static final String ROLE_VOLUNTEER = "ROLE_VOLUNTEER";
    // 救助站工作人员
    public static final String ROLE_WORKER = "ROLE_WORKER";
    // 捐赠者
    public static final String ROLE_DONOR = "ROLE_DONOR";
    // 兽医
    public static final String ROLE_VETERINARIAN = "ROLE_VETERINARIAN";
    // 超级管理员
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final Set<String> ALL_ROLES = Set.of(
            ROLE_NORMAL,
            ROLE_VOLUNTEER,
            ROLE_WORKER,
            ROLE_DONOR,
            ROLE_VETERINARIAN,
            ROLE_ADMIN);

    public static final int MASK_VOLUNTEER = 0x1;
    public static final int MASK_WORKER = 0x2;
    public static final int MASK_DONOR = 0x4;
    public static final int MASK_VETERINARIAN = 0x8;
    public static final int MASK_ADMIN = 0x10;
    public static final Map<String, Integer> MATCH_MASK_MAP = Map.of(
            ROLE_NORMAL, 0x1F, // Match ALL
            ROLE_VOLUNTEER, MASK_VOLUNTEER,
            ROLE_WORKER, MASK_WORKER,
            ROLE_DONOR, MASK_DONOR,
            ROLE_VETERINARIAN, MASK_VETERINARIAN,
            ROLE_ADMIN, MASK_ADMIN);

    private AuthenticationManager authenticationManager;

    /**
     * 获取当前登录用户
     */
    public Optional<User> getLoginUserOpt() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(auth -> (CustomUserDetails) auth.getPrincipal())
                .map(CustomUserDetails::getUser);
    }

    /**
     * 获取当前登录用户
     */
    public User getLoginUser(Function<String, ? extends RuntimeException> errorHandler) {
        return getLoginUserOpt().orElseThrow(() -> errorHandler.apply("请先登录"));
    }

    /**
     * 登录
     *
     * @param username 用户名
     * @param password 密码
     */
    public CustomUserDetails authenticate(String username, String password) {
        Authentication token = new UsernamePasswordAuthenticationToken(username, password);
        Authentication authentication = authenticationManager.authenticate(token);
        return (CustomUserDetails) authentication.getPrincipal();
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

    public static Integer getRoleCode(IUserRole user) {
        return getRoleCode(user.getRole());
    }

    @Autowired
    @Lazy
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }
}
