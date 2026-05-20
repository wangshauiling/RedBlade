package com.redblade.auth.annotation;

import java.lang.annotation.*;

/**
 * 权限校验注解
 * 标记在方法上，表示需要特定权限才能访问
 *
 * 使用示例：
 * <pre>
 * &#64;RequiresPermission("system:user:add")
 * public void addUser(UserEntity user) { ... }
 *
 * &#64;RequiresPermission(value = {"system:user:edit", "system:user:add"}, logical = Logical.OR)
 * public void updateUser(UserEntity user) { ... }
 * </pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresPermission {

    /**
     * 权限标识列表
     */
    String[] value();

    /**
     * 逻辑关系（AND/OR）
     */
    Logical logical() default Logical.AND;
}