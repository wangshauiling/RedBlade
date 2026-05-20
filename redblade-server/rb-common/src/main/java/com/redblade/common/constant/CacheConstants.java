package com.redblade.common.constant;

/**
 * 缓存Key常量
 */
public interface CacheConstants {

    /**
     * 缓存前缀
     */
    String PREFIX = "rb:";

    /**
     * 用户登录Token
     */
    String LOGIN_TOKEN_KEY = PREFIX + "login:token:";

    /**
     * 用户信息
     */
    String USER_INFO_KEY = PREFIX + "user:info:";

    /**
     * 验证码
     */
    String CAPTCHA_KEY = PREFIX + "captcha:";

    /**
     * 用户权限
     */
    String USER_PERMISSION_KEY = PREFIX + "user:permission:";

    /**
     * 用户菜单
     */
    String USER_MENU_KEY = PREFIX + "user:menu:";
}