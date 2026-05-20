package com.redblade.auth.annotation;

/**
 * 逻辑关系枚举
 */
public enum Logical {
    /**
     * 必须具有所有权限
     */
    AND,

    /**
     * 只需具有其中一个权限
     */
    OR
}
