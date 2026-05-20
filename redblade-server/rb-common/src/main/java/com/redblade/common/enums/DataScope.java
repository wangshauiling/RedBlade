package com.redblade.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据权限范围枚举
 */
@Getter
@AllArgsConstructor
public enum DataScope {

    /**
     * 全部数据权限
     */
    ALL(1, "全部数据权限"),

    /**
     * 自定义数据权限
     */
    CUSTOM(2, "自定义数据权限"),

    /**
     * 本部门数据权限
     */
    DEPT(3, "本部门数据权限"),

    /**
     * 本部门及以下数据权限
     */
    DEPT_AND_CHILD(4, "本部门及以下数据权限"),

    /**
     * 仅本人数据权限
     */
    SELF(5, "仅本人数据权限");

    private final int scope;
    private final String desc;
}