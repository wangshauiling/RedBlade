package com.redblade.auth.service;

import com.redblade.auth.domain.LoginRequest;
import com.redblade.auth.domain.LoginResponse;
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