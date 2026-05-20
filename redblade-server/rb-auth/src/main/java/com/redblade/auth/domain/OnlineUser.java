package com.redblade.auth.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 在线用户信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnlineUser {

    /**
     * 用户编码
     */
    private String userCode;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 组织编码
     */
    private String orgCode;

    /**
     * 组织名称
     */
    private String orgName;

    /**
     * 部门编码
     */
    private String deptCode;

    /**
     * IP地址
     */
    private String ip;

    /**
     * 登录地点
     */
    private String location;

    /**
     * 浏览器
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 登录时间
     */
    private LocalDateTime loginTime;

    /**
     * Token
     */
    private String token;

    /**
     * Token过期时间
     */
    private LocalDateTime expireTime;
}