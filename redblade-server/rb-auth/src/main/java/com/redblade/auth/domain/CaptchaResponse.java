package com.redblade.auth.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 验证码响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaptchaResponse {

    /**
     * 验证码Key（用于验证时提交）
     */
    private String captchaKey;

    /**
     * 验证码图片（Base64编码）
     */
    private String captchaImage;

    /**
     * 是否启用验证码
     */
    private boolean captchaEnabled;
}