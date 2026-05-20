package com.redblade.auth.annotation;

import java.lang.annotation.*;

/**
 * 角色校验注解
 * 标记在方法上，表示需要特定角色才能访问
 *
 * 使用示例：
 * <pre>
 * &#64;RequiresRole("admin")
 * public void deleteUser(Long userId) { ... }
 * </pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresRole {

    /**
     * 角色标识列表
     */
    String[] value();

    /**
     * 逻辑关系（AND/OR）
     */
    Logical logical() default Logical.AND;
}