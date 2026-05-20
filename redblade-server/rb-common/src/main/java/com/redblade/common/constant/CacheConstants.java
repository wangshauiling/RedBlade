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

    /**
     * 登录失败次数
     */
    String LOGIN_FAIL_COUNT_KEY = PREFIX + "login:fail:";

    /**
     * 用户锁定状态
     */
    String USER_LOCK_KEY = PREFIX + "user:lock:";

    /**
     * 在线用户列表
     */
    String ONLINE_USER_KEY = PREFIX + "online:user:";
}