package com.example.backend.util;

import com.example.backend.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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

    public static int MASK_VOLUNTEER = 0x1;
    public static int MASK_WORKER = 0x2;
    public static int MASK_DONOR = 0x4;
    public static int MASK_VETERINARIAN = 0x8;
    public static int MASK_ADMIN = 0x10;

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
     * @param username 用户名
     * @param password 密码
     */
    public CustomUserDetails authenticate(String username, String password) {
        Authentication token = new UsernamePasswordAuthenticationToken(username, password);
        Authentication authentication = authenticationManager.authenticate(token);
        return (CustomUserDetails) authentication.getPrincipal();
    }

    public Collection<GrantedAuthority> createAuthorities(int role) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority(ROLE_NORMAL));
        if ((role & MASK_VOLUNTEER) == MASK_VOLUNTEER)
            // 志愿者
            authorities.add(new SimpleGrantedAuthority(ROLE_VOLUNTEER));
        if ((role & MASK_WORKER) == MASK_WORKER) {
            // 救助站工作人员
            authorities.add(new SimpleGrantedAuthority(ROLE_VOLUNTEER));
            authorities.add(new SimpleGrantedAuthority(ROLE_WORKER));
        }
        if ((role & MASK_DONOR) == MASK_DONOR)
            // 捐赠者
            authorities.add(new SimpleGrantedAuthority(ROLE_DONOR));
        if ((role & MASK_VETERINARIAN) == MASK_VETERINARIAN)
            // 兽医
            authorities.add(new SimpleGrantedAuthority(ROLE_VETERINARIAN));
        if ((role & MASK_ADMIN) == MASK_ADMIN) {
            // 超级管理员
            authorities.add(new SimpleGrantedAuthority(ROLE_VOLUNTEER));
            authorities.add(new SimpleGrantedAuthority(ROLE_WORKER));
            authorities.add(new SimpleGrantedAuthority(ROLE_DONOR));
            authorities.add(new SimpleGrantedAuthority(ROLE_VETERINARIAN));
            authorities.add(new SimpleGrantedAuthority(ROLE_ADMIN));
        }
        return new ArrayList<>(authorities);
    }

    public boolean isVolunteer(User user) {
        int role = user.getRole();
        return (role & MASK_VOLUNTEER) == MASK_VOLUNTEER || isWorker(role);
    }

    public boolean isWorker(int role) {
        return (role & MASK_WORKER) == MASK_WORKER || (role & MASK_ADMIN) == MASK_ADMIN;
    }

    public boolean isWorker(User user) {
        return isWorker(user.getRole());
    }

    public boolean isAdmin(int role) {
        return (role & MASK_ADMIN) == MASK_ADMIN;
    }

    public boolean isAdmin(User user) {
        return isAdmin(user.getRole());
    }

    @Autowired
    @Lazy
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }
}
