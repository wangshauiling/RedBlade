package com.redblade.init.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 初始化配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "rb.init")
public class InitProperties {

    /**
     * 是否启用初始化
     * 默认禁用，只有在使用 -db 参数启动时才启用
     */
    private boolean enabled = false;

    /**
     * 初始化模式
     * none: 不初始化
     * create-if-not-exists: 表不存在时创建（默认）
     * always: 每次启动都检查并更新
     */
    private InitMode mode = InitMode.CREATE_IF_NOT_EXISTS;

    /**
     * 是否先删后建（危险，仅开发环境使用）
     */
    private boolean dropBeforeCreate = false;

    /**
     * 版本控制表名
     */
    private String versionTable = "sys_init_version";

    /**
     * 数据库类型
     */
    private String databaseType = "postgresql";

    /**
     * 初始化模式枚举
     */
    public enum InitMode {
        NONE,
        CREATE_IF_NOT_EXISTS,
        ALWAYS
    }
}