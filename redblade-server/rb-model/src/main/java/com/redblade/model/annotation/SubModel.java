package com.redblade.model.annotation;

import java.lang.annotation.*;

/**
 * 从表注解
 * 用于主从表关联配置
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SubModel {

    /**
     * 从表名称
     */
    String name();

    /**
     * 从表表名
     */
    String table();

    /**
     * 外键字段
     */
    String foreignKey();

    /**
     * 从表实体类
     */
    Class<?> entity() default void.class;

    /**
     * 级联删除
     */
    boolean cascadeDelete() default true;
}