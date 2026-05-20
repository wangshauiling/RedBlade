package com.redblade.common.helper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 通用工具助手
 * 提供常用的工具方法
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommonHelper {

    /**
     * 生成 UUID（无横线）
     */
    public String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成 UUID（带横线）
     */
    public String uuidWithDash() {
        return UUID.randomUUID().toString();
    }

    /**
     * 获取当前时间
     */
    public LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * 获取当前时间字符串
     */
    public String nowStr() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 获取当前时间字符串（指定格式）
     */
    public String nowStr(String pattern) {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 生成单据编号
     *
     * @param prefix 前缀（如 "RO" 表示出库单）
     * @return 单据编号（如 RO20240520001）
     */
    public String generateDocNo(String prefix) {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = String.format("%03d", (int) (Math.random() * 1000));
        return prefix + dateStr + random;
    }

    /**
     * 判断字符串是否为空
     */
    public boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 判断字符串是否不为空
     */
    public boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 首字母大写
     */
    public String capitalize(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * 驼峰转下划线
     */
    public String camelToSnake(String str) {
        if (isEmpty(str)) {
            return str;
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c)) {
                result.append('_').append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * 下划线转驼峰
     */
    public String snakeToCamel(String str) {
        if (isEmpty(str)) {
            return str;
        }
        StringBuilder result = new StringBuilder();
        boolean nextUpper = false;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '_') {
                nextUpper = true;
            } else {
                if (nextUpper) {
                    result.append(Character.toUpperCase(c));
                    nextUpper = false;
                } else {
                    result.append(c);
                }
            }
        }
        return result.toString();
    }
}
