package com.redblade.init.metadata.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 数据库元数据注解
 * 标记在类上，表示该类提供初始化数据
 *
 * 使用示例：
 * <pre>
 * &#64;DbMetaData(table = "sys_menu", order = 10)
 * public class MenuInitData extends BaseDml&lt;MenuDto&gt; {
 *     public MenuInitData() {
 *         add(new MenuDto("system.001", 1, "用户管理", "/system/user", ...));
 *         add(new MenuDto("system.002", 2, "角色管理", "/system/role", ...));
 *     }
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface DbMetaData {

    /**
     * 目标表名
     */
    String table();

    /**
     * 执行顺序（越小越先执行）
     */
    int order() default 100;

    /**
     * 是否幂等（已存在则跳过）
     */
    boolean idempotent() default true;

    /**
     * 唯一键字段（用于幂等判断）
     */
    String[] uniqueKeys() default {};

    /**
     * 描述说明
     */
    String description() default "";
}
