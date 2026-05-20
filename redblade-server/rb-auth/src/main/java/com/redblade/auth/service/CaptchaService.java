package com.redblade.auth.service;

import com.redblade.auth.domain.CaptchaResponse;

/**
 * 验证码服务接口
 */
public interface CaptchaService {

    /**
     * 生成验证码
     *
     * @return 验证码响应
     */
    CaptchaResponse generateCaptcha();

    /**
     * 验证验证码
     *
     * @param captchaKey 验证码Key
     * @param captcha 验证码
     * @return 是否验证成功
     */
    boolean validateCaptcha(String captchaKey, String captcha);

    /**
     * 是否启用验证码
     *
     * @return 是否启用
     */
    boolean isCaptchaEnabled();
}