package com.redblade.init.data;

import java.util.List;
import java.util.Map;

/**
 * 初始数据加载接口
 */
public interface InitDataLoader {

    /**
     * 获取表名
     */
    String getTableName();

    /**
     * 获取加载顺序（越小越先加载）
     */
    default int getOrder() {
        return 100;
    }

    /**
     * 获取初始数据
     */
    List<Map<String, Object>> getData();

    /**
     * 是否幂等（已存在则跳过）
     */
    default boolean isIdempotent() {
        return true;
    }

    /**
     * 唯一键字段（用于幂等判断）
     */
    default List<String> getUniqueKeys() {
        return List.of();
    }
}