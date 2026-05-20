package com.redblade.database.annotation;

import java.lang.annotation.*;

/**
 * 数据权限注解
 * 用于标记需要进行数据权限过滤的方法或类
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {

    /**
     * 数据权限字段名，默认为 org_code
     */
    String field() default "org_code";

    /**
     * 是否启用数据权限过滤，默认启用
     */
    boolean enabled() default true;
}
