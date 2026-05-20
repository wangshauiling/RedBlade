package com.redblade.i18n.constant;

/**
 * 语言常量
 */
public interface LangConstants {

    /**
     * 默认语言
     */
    String DEFAULT_LANG = "zh-CN";

    /**
     * 简体中文
     */
    String ZH_CN = "zh-CN";

    /**
     * 繁体中文
     */
    String ZH_TW = "zh-TW";

    /**
     * English
     */
    String EN_US = "en-US";

    /**
     * 日本語
     */
    String JA_JP = "ja-JP";

    /**
     * 한국어
     */
    String KO_KR = "ko-KR";

    /**
     * Tiếng Việt
     */
    String VI_VN = "vi-VN";

    /**
     * 支持的语言列表
     */
    String[] SUPPORTED_LANGS = {ZH_CN, ZH_TW, EN_US, JA_JP, KO_KR, VI_VN};

    /**
     * 请求头名称
     */
    String HEADER_ACCEPT_LANGUAGE = "Accept-Language";

    /**
     * Cookie 名称
     */
    String COOKIE_LANG_KEY = "rb_lang";
}