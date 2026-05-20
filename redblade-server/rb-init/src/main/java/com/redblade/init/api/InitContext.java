package com.redblade.init.api;

import com.redblade.common.enums.DbType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 初始化上下文
 * 在初始化过程中传递共享数据
 */
@Data
public class InitContext {

    /**
     * 数据库类型
     */
    private DbType dbType;

    /**
     * 是否首次运行（无组织数据）
     */
    private boolean firstRun;

    /**
     * 组织编码（交互式输入或配置）
     */
    private String orgCode;

    /**
     * 组织名称
     */
    private String orgName;

    /**
     * 管理员用户名
     */
    private String adminUsername;

    /**
     * 管理员密码
     */
    private String adminPassword;

    /**
     * 初始化开始时间
     */
    private LocalDateTime startTime;

    /**
     * 创建的表数量
     */
    private int tablesCreated;

    /**
     * 跳过的表数量
     */
    private int tablesSkipped;

    /**
     * 扩展数据存储
     * 用于扩展点之间传递自定义数据
     */
    private Map<String, Object> attributes = new HashMap<>();

    /**
     * 设置扩展属性
     */
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    /**
     * 获取扩展属性
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        return (T) attributes.get(key);
    }

    /**
     * 获取扩展属性（带默认值）
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key, T defaultValue) {
        Object value = attributes.get(key);
        return value != null ? (T) value : defaultValue;
    }
}