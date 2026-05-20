package com.redblade.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应状态码枚举
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    /**
     * 成功
     */
    SUCCESS(200, "操作成功"),

    /**
     * 失败
     */
    FAIL(500, "操作失败"),

    /**
     * 参数错误
     */
    PARAM_ERROR(400, "参数错误"),

    /**
     * 未认证
     */
    UNAUTHORIZED(401, "未认证或认证已过期"),

    /**
     * 无权限
     */
    FORBIDDEN(403, "无权限访问"),

    /**
     * 资源不存在
     */
    NOT_FOUND(404, "资源不存在"),

    /**
     * 用户名或密码错误
     */
    LOGIN_ERROR(4001, "用户名或密码错误"),

    /**
     * 账号已被停用
     */
    ACCOUNT_DISABLED(4002, "账号已被停用"),

    /**
     * Token无效
     */
    TOKEN_INVALID(4003, "Token无效"),

    /**
     * Token已过期
     */
    TOKEN_EXPIRED(4004, "Token已过期"),

    /**
     * 数据已存在
     */
    DATA_EXIST(5001, "数据已存在"),

    /**
     * 数据不存在
     */
    DATA_NOT_EXIST(5002, "数据不存在");

    private final int code;
    private final String msg;
}