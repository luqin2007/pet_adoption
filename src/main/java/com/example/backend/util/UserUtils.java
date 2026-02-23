package com.example.backend.util;

import com.example.backend.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class UserUtils {

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

    public static Collection<GrantedAuthority> createAuthorities(int role) {
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

    public static boolean isWorker(int role) {
        return (role & MASK_WORKER) == MASK_WORKER || (role & MASK_ADMIN) == MASK_ADMIN;
    }

    public static boolean isAdmin(int role) {
        return (role & MASK_ADMIN) == MASK_ADMIN;
    }
}
