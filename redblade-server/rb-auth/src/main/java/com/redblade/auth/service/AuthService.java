package com.redblade.auth.service;

import com.redblade.auth.domain.ChangePasswordRequest;
import com.redblade.auth.domain.LoginRequest;
import com.redblade.auth.domain.LoginResponse;
import com.redblade.auth.domain.RegisterRequest;
import com.redblade.auth.security.LoginUser;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 登录
     *
     * @param request 登录请求
     * @return 登录响应
     */
    LoginResponse login(LoginRequest request);

    /**
     * 登出
     */
    void logout();

    /**
     * 刷新令牌
     *
     * @param refreshToken 刷新令牌
     * @return 新的登录响应
     */
    LoginResponse refresh(String refreshToken);

    /**
     * 注册
     *
     * @param request 注册请求
     * @return 登录响应
     */
    LoginResponse register(RegisterRequest request);

    /**
     * 修改密码
     *
     * @param request 修改密码请求
     */
    void changePassword(ChangePasswordRequest request);

    /**
     * 获取当前登录用户
     *
     * @return 登录用户信息
     */
    LoginUser getCurrentUser();

    /**
     * 获取当前用户ID
     */
    Long getCurrentUserId();

    /**
     * 获取当前用户编码
     */
    String getCurrentUserCode();

    /**
     * 获取当前组织编码
     */
    String getCurrentOrgCode();
}