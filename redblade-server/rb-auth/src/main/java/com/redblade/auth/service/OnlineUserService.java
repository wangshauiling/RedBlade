package com.redblade.auth.service;

import com.redblade.auth.domain.OnlineUser;
import com.redblade.common.domain.PageResult;

import java.util.List;

/**
 * 在线用户服务接口
 */
public interface OnlineUserService {

    /**
     * 获取在线用户列表
     *
     * @param orgCode 组织编码（可选）
     * @param username 用户名（可选）
     * @return 在线用户列表
     */
    List<OnlineUser> getOnlineUsers(String orgCode, String username);

    /**
     * 强制下线用户
     *
     * @param token 用户Token
     */
    void kickout(String token);

    /**
     * 批量强制下线
     *
     * @param tokens Token列表
     */
    void kickoutBatch(List<String> tokens);

    /**
     * 获取在线用户数量
     *
     * @return 在线用户数量
     */
    long getOnlineCount();
}