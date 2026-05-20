package com.redblade.i18n.service;

/**
 * 国际化服务接口
 */
public interface I18nService {

    /**
     * 获取当前语言
     */
    String getCurrentLang();

    /**
     * 获取消息
     *
     * @param code 消息代码
     * @return 消息内容
     */
    String getMessage(String code);

    /**
     * 获取消息
     *
     * @param code 消息代码
     * @param args 参数
     * @return 消息内容
     */
    String getMessage(String code, Object... args);

    /**
     * 获取指定语言的消息
     *
     * @param code 消息代码
     * @param lang 语言
     * @return 消息内容
     */
    String getMessageByLang(String code, String lang);

    /**
     * 获取指定语言的消息
     *
     * @param code 消息代码
     * @param args 参数
     * @param lang 语言
     * @return 消息内容
     */
    String getMessageByLang(String code, Object[] args, String lang);

    /**
     * 获取消息，带默认值
     *
     * @param code         消息代码
     * @param defaultMessage 默认消息
     * @return 消息内容
     */
    String getMessageWithDefault(String code, String defaultMessage);
}