package com.redblade.init.metadata;

import java.util.List;

/**
 * 表定义接口
 */
public interface TableDefinition {

    /**
     * 获取表名
     */
    String getTableName();

    /**
     * 获取表注释
     */
    String getComment();

    /**
     * 获取字段列表
     */
    List<ColumnDefinition> getColumns();

    /**
     * 获取索引列表
     */
    default List<IndexDefinition> getIndexes() {
        return List.of();
    }

    /**
     * 获取版本号（用于增量更新）
     */
    default int getVersion() {
        return 1;
    }

    /**
     * 是否需要初始化数据
     */
    default boolean needInitData() {
        return false;
    }
}