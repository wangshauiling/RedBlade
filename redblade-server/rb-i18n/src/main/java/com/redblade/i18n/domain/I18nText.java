package com.redblade.i18n.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 多语言文本对象
 * 用于存储和获取多语言内容
 */
@Data
public class I18nText implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 简体中文
     */
    private String zhCN;

    /**
     * 繁体中文
     */
    private String zhTW;

    /**
     * English
     */
    private String enUS;

    /**
     * 日本語
     */
    private String jaJP;

    /**
     * 한국어
     */
    private String koKR;

    /**
     * Tiếng Việt
     */
    private String viVN;

    public I18nText() {
    }

    public I18nText(String text) {
        this.zhCN = text;
    }

    /**
     * 根据语言代码获取文本
     */
    public String get(String lang) {
        return switch (lang) {
            case "zh-CN" -> zhCN;
            case "zh-TW" -> zhTW != null ? zhTW : zhCN;
            case "en-US" -> enUS != null ? enUS : zhCN;
            case "ja-JP" -> jaJP != null ? jaJP : zhCN;
            case "ko-KR" -> koKR != null ? koKR : zhCN;
            case "vi-VN" -> viVN != null ? viVN : zhCN;
            default -> zhCN;
        };
    }

    /**
     * 设置指定语言的文本
     */
    public void set(String lang, String text) {
        switch (lang) {
            case "zh-CN" -> zhCN = text;
            case "zh-TW" -> zhTW = text;
            case "en-US" -> enUS = text;
            case "ja-JP" -> jaJP = text;
            case "ko-KR" -> koKR = text;
            case "vi-VN" -> viVN = text;
        }
    }

    /**
     * 转换为 Map
     */
    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();
        if (zhCN != null) map.put("zh-CN", zhCN);
        if (zhTW != null) map.put("zh-TW", zhTW);
        if (enUS != null) map.put("en-US", enUS);
        if (jaJP != null) map.put("ja-JP", jaJP);
        if (koKR != null) map.put("ko-KR", koKR);
        if (viVN != null) map.put("vi-VN", viVN);
        return map;
    }

    /**
     * 从 Map 创建
     */
    public static I18nText fromMap(Map<String, String> map) {
        if (map == null) return null;
        I18nText text = new I18nText();
        text.setZhCN(map.get("zh-CN"));
        text.setZhTW(map.get("zh-TW"));
        text.setEnUS(map.get("en-US"));
        text.setJaJP(map.get("ja-JP"));
        text.setKoKR(map.get("ko-KR"));
        text.setViVN(map.get("vi-VN"));
        return text;
    }

    /**
     * 创建仅包含简体中文的文本
     */
    public static I18nText of(String text) {
        I18nText i18nText = new I18nText();
        i18nText.setZhCN(text);
        return i18nText;
    }

    /**
     * 创建包含多种语言的文本
     */
    public static I18nText of(String zhCN, String enUS) {
        I18nText i18nText = new I18nText();
        i18nText.setZhCN(zhCN);
        i18nText.setEnUS(enUS);
        return i18nText;
    }
}