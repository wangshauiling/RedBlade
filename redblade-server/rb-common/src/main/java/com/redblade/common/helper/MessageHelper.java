package com.redblade.common.helper;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * 消息助手
 * 支持国际化消息获取
 *
 * 使用示例：
 * <pre>
 * // 获取简单消息
 * String msg = messageHelper.get("compal.msg.0013");
 *
 * // 获取带参数的消息
 * String msg = messageHelper.getAsFormat("compal.msg.0013", "参数1", "参数2");
 *
 * // 获取带默认值的消息
 * String msg = messageHelper.get("compal.msg.0013", "默认消息");
 * </pre>
 */
@Component
@RequiredArgsConstructor
public class MessageHelper {

    private final MessageSource messageSource;

    /**
     * 获取消息
     *
     * @param code 消息编码
     * @return 消息内容
     */
    public String get(String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }

    /**
     * 获取消息（带默认值）
     *
     * @param code         消息编码
     * @param defaultValue 默认值
     * @return 消息内容
     */
    public String get(String code, String defaultValue) {
        return messageSource.getMessage(code, null, defaultValue, LocaleContextHolder.getLocale());
    }

    /**
     * 获取带参数的消息
     *
     * @param code  消息编码
     * @param args  参数
     * @return 格式化后的消息
     */
    public String getAsFormat(String code, Object... args) {
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }

    /**
     * 获取带参数的消息（带默认值）
     *
     * @param code         消息编码
     * @param defaultValue 默认值
     * @param args         参数
     * @return 格式化后的消息
     */
    public String getAsFormat(String code, String defaultValue, Object... args) {
        return messageSource.getMessage(code, args, defaultValue, LocaleContextHolder.getLocale());
    }

    /**
     * 获取消息（指定语言）
     *
     * @param code   消息编码
     * @param locale 语言
     * @return 消息内容
     */
    public String get(String code, Locale locale) {
        return messageSource.getMessage(code, null, locale);
    }

    /**
     * 获取带参数的消息（指定语言）
     *
     * @param code   消息编码
     * @param locale 语言
     * @param args   参数
     * @return 格式化后的消息
     */
    public String getAsFormat(String code, Locale locale, Object... args) {
        return messageSource.getMessage(code, args, locale);
    }

    /**
     * 判断消息是否存在
     *
     * @param code 消息编码
     * @return true 存在
     */
    public boolean exists(String code) {
        try {
            messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
