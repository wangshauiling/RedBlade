package com.redblade.init.metadata.annotation;

import java.lang.annotation.*;

/**
 * 表结构定义注解
 * 标记在类上，表示该类定义表结构
 *
 * 使用示例：
 * <pre>
 * &#64;DbTable(name = "sys_user", comment = "用户表", version = 1)
 * public class SysUserTable extends BaseTableDefinition {
 *     &#64;Override
 *     protected void defineColumns() {
 *         column("org_code", DataType.VARCHAR, 50).primaryKey().comment("组织编码");
 *         column("user_id", DataType.BIGINT).primaryKey().autoIncrement().comment("用户ID");
 *         column("username", DataType.VARCHAR, 50).nullable(false).comment("用户名");
 *         ...
 *     }
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DbTable {

    /**
     * 表名
     */
    String name();

    /**
     * 表注释
     */
    String comment() default "";

    /**
     * 版本号（用于增量更新）
     */
    int version() default 1;

    /**
     * 是否包含组织编码字段（自动添加 org_code 作为联合主键）
     */
    boolean orgCode() default false;

    /**
     * 是否包含逻辑删除字段（自动添加 del_flag）
     */
    boolean logicDelete() default false;

    /**
     * 是否包含审计字段（自动添加 create_time, update_time 等）
     */
    boolean audit() default false;

    /**
     * 描述说明
     */
    String description() default "";
}