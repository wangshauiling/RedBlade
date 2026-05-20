package com.redblade.auth.service.impl;

import com.redblade.auth.security.LoginUser;
import com.redblade.auth.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Set;

/**
 * 权限服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    /**
     * 超级管理员角色
     */
    private static final String SUPER_ADMIN = "admin";

    @Override
    public boolean hasPermission(String permission) {
        LoginUser user = getLoginUser();
        if (user == null) {
            return false;
        }

        // 超级管理员拥有所有权限
        if (isSuperAdmin()) {
            return true;
        }

        Set<String> permissions = user.getPermissions();
        return permissions != null && permissions.contains(permission);
    }

    @Override
    public boolean hasAnyPermission(String... permissions) {
        for (String permission : permissions) {
            if (hasPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasRole(String role) {
        LoginUser user = getLoginUser();
        if (user == null) {
            return false;
        }

        Set<String> roles = user.getRoles();
        return roles != null && roles.contains(role);
    }

    @Override
    public boolean hasAnyRole(String... roles) {
        for (String role : roles) {
            if (hasRole(role)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isSuperAdmin() {
        LoginUser user = getLoginUser();
        if (user == null) {
            return false;
        }

        Set<String> roles = user.getRoles();
        return roles != null && roles.contains(SUPER_ADMIN);
    }

    @Override
    public LoginUser getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
            return (LoginUser) authentication.getPrincipal();
        }
        return null;
    }
}
