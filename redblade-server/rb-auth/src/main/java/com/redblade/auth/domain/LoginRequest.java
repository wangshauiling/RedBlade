package com.redblade.auth.domain;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求
 */
@Data
public class LoginRequest {

    /**
     * 组织编码
     */
    @NotBlank(message = "组织编码不能为空")
    private String orgCode;

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 验证码（可选）
     */
    private String captcha;

    /**
     * 验证码Key（可选）
     */
    private String captchaKey;
}
