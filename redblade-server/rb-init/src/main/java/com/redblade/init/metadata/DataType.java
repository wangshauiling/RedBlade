package com.redblade.init.metadata;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据类型枚举
 */
@Getter
@AllArgsConstructor
public enum DataType {

    /**
     * 字符串
     */
    VARCHAR("VARCHAR"),

    /**
     * 定长字符串
     */
    CHAR("CHAR"),

    /**
     * 长文本
     */
    TEXT("TEXT"),

    /**
     * 整数
     */
    INT("INT"),

    /**
     * 长整数
     */
    BIGINT("BIGINT"),

    /**
     * 小整数
     */
    SMALLINT("SMALLINT"),

    /**
     * 浮点数
     */
    FLOAT("FLOAT"),

    /**
     * 双精度浮点数
     */
    DOUBLE("DOUBLE"),

    /**
     * 十进制数
     */
    DECIMAL("DECIMAL"),

    /**
     * 日期时间
     */
    DATETIME("DATETIME"),

    /**
     * 日期
     */
    DATE("DATE"),

    /**
     * 时间
     */
    TIME("TIME"),

    /**
     * 布尔
     */
    BOOLEAN("BOOLEAN"),

    /**
     * 大对象
     */
    BLOB("BLOB"),

    /**
     * 大文本对象
     */
    CLOB("CLOB");

    private final String name;
}