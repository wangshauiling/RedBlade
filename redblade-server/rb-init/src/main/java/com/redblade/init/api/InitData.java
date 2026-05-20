package com.redblade.init.api;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 初始数据定义
 * 用于在初始化时插入数据
 */
@Data
public class InitData {

    /**
     * 表名
     */
    private String tableName;

    /**
     * 数据列表
     */
    private List<Map<String, Object>> data;

    /**
     * 加载顺序
     */
    private int order = 100;

    /**
     * 是否幂等（已存在则跳过）
     */
    private boolean idempotent = true;

    /**
     * 唯一键字段（用于幂等判断）
     */
    private List<String> uniqueKeys;

    /**
     * 创建初始数据
     */
    public static InitData of(String tableName, List<Map<String, Object>> data) {
        InitData initData = new InitData();
        initData.setTableName(tableName);
        initData.setData(data);
        return initData;
    }

    /**
     * 创建单条初始数据
     */
    public static InitData ofSingle(String tableName, Map<String, Object> data) {
        return of(tableName, List.of(data));
    }

    /**
     * 设置顺序
     */
    public InitData order(int order) {
        this.order = order;
        return this;
    }

    /**
     * 设置唯一键
     */
    public InitData uniqueKeys(List<String> uniqueKeys) {
        this.uniqueKeys = uniqueKeys;
        return this;
    }
}