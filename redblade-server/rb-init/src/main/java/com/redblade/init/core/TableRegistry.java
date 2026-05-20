package com.redblade.init.core;

import com.redblade.init.metadata.TableDefinition;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 表注册中心
 */
@Component
public class TableRegistry {

    private final Map<String, TableDefinition> tableMap = new ConcurrentHashMap<>();

    /**
     * 注册表定义
     */
    public void register(TableDefinition tableDefinition) {
        tableMap.put(tableDefinition.getTableName(), tableDefinition);
    }

    /**
     * 获取表定义
     */
    public TableDefinition get(String tableName) {
        return tableMap.get(tableName);
    }

    /**
     * 获取所有表定义
     */
    public Collection<TableDefinition> getAll() {
        return tableMap.values();
    }

    /**
     * 按版本排序获取所有表定义
     */
    public List<TableDefinition> getAllSortedByVersion() {
        return tableMap.values().stream()
            .sorted(Comparator.comparingInt(TableDefinition::getVersion))
            .toList();
    }

    /**
     * 是否存在表定义
     */
    public boolean contains(String tableName) {
        return tableMap.containsKey(tableName);
    }

    /**
     * 获取表数量
     */
    public int size() {
        return tableMap.size();
    }
}