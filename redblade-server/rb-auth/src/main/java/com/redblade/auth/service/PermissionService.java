package com.redblade.auth.service;

import com.redblade.auth.security.LoginUser;

/**
 * 权限服务接口
 */
public interface PermissionService {

    /**
     * 检查是否有权限
     *
     * @param permission 权限标识
     * @return true 有权限
     */
    boolean hasPermission(String permission);

    /**
     * 检查是否有任一权限
     *
     * @param permissions 权限标识列表
     * @return true 有任一权限
     */
    boolean hasAnyPermission(String... permissions);

    /**
     * 检查是否有角色
     *
     * @param role 角色标识
     * @return true 有角色
     */
    boolean hasRole(String role);

    /**
     * 检查是否有任一角色
     *
     * @param roles 角色标识列表
     * @return true 有任一角色
     */
    boolean hasAnyRole(String... roles);

    /**
     * 检查是否是超级管理员
     */
    boolean isSuperAdmin();

    /**
     * 获取当前登录用户
     */
    LoginUser getLoginUser();
}
