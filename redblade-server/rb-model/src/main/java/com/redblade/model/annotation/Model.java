package com.redblade.model.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * Model 注解
 * 标注一个类为业务 Model，自动注册 REST API
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Model {

    /**
     * Model 名称（用于菜单显示）
     */
    String name();

    /**
     * 数据库表名
     */
    String table();

    /**
     * API 路径前缀
     * 完整路径: /api/model/{api}
     */
    String api() default "";

    /**
     * 实体类
     */
    Class<?> entity() default void.class;

    /**
     * 是否启用组织隔离
     */
    boolean orgIsolation() default true;

    /**
     * 是否启用逻辑删除
     */
    boolean logicDelete() default true;

    /**
     * 是否启用审计字段（创建时间、更新时间等）
     */
    boolean audit() default true;

    /**
     * 排序字段
     */
    String sortField() default "create_time";

    /**
     * 排序方式（asc/desc）
     */
    String sortOrder() default "desc";
}