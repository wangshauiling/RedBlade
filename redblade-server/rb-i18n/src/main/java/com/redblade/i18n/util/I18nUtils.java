package com.redblade.i18n.util;

import com.redblade.i18n.service.I18nService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * 国际化工具类
 * 静态方法获取国际化消息
 */
@Component
public class I18nUtils implements ApplicationContextAware {

    private static I18nService i18nService;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        i18nService = applicationContext.getBean(I18nService.class);
    }

    /**
     * 获取消息
     */
    public static String getMessage(String code) {
        return i18nService.getMessage(code);
    }

    /**
     * 获取消息
     */
    public static String getMessage(String code, Object... args) {
        return i18nService.getMessage(code, args);
    }

    /**
     * 获取消息
     */
    public static String getMessage(String code, String lang) {
        return i18nService.getMessage(code, lang);
    }

    /**
     * 获取消息
     */
    public static String getMessage(String code, Object[] args, String lang) {
        return i18nService.getMessage(code, args, lang);
    }

    /**
     * 获取当前语言
     */
    public static String getCurrentLang() {
        return i18nService.getCurrentLang();
    }
}