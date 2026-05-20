package com.redblade.model.annotation;

import java.lang.annotation.*;

/**
 * 业务方法注解
 * 用于标注自定义业务方法，自动暴露为 API
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ModelMethod {

    /**
     * 方法名称
     */
    String name() default "";

    /**
     * API 路径（默认使用方法名）
     */
    String path() default "";

    /**
     * HTTP 方法
     */
    HttpMethod method() default HttpMethod.POST;

    /**
     * 权限标识
     */
    String permission() default "";

    /**
     * 是否需要事务
     */
    boolean transactional() default false;

    enum HttpMethod {
        GET, POST, PUT, DELETE
    }
}