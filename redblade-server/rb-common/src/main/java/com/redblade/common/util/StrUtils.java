package com.redblade.common.util;

import cn.hutool.core.util.StrUtil;

/**
 * 字符串工具类（扩展Hutool）
 */
public class StrUtils extends StrUtil {

    /**
     * 判断是否为空字符串
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }

    /**
     * 判断是否不为空字符串
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 截取字符串（安全处理）
     */
    public static String truncate(String str, int maxLength) {
        if (isEmpty(str) || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength);
    }
}