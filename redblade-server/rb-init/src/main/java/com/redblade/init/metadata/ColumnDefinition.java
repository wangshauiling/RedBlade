package com.redblade.init.metadata;

import lombok.Builder;
import lombok.Data;

/**
 * 字段定义
 */
@Data
@Builder
public class ColumnDefinition {

    /**
     * 字段名
     */
    private String name;

    /**
     * 数据类型
     */
    private DataType type;

    /**
     * 长度
     */
    private Integer length;

    /**
     * 小数位数
     */
    private Integer scale;

    /**
     * 是否主键
     */
    private boolean primaryKey;

    /**
     * 是否自增
     */
    private boolean autoIncrement;

    /**
     * 是否允许为空
     */
    private boolean nullable;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 字段注释
     */
    private String comment;
}