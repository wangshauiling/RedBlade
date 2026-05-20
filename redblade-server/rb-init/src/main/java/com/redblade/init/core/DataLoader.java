package com.redblade.init.core;

import com.redblade.init.data.InitDataLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 初始数据加载器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataLoader {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 加载初始数据
     */
    public void loadData(InitDataLoader dataLoader) {
        String tableName = dataLoader.getTableName();
        List<Map<String, Object>> dataList = dataLoader.getData();

        if (dataList == null || dataList.isEmpty()) {
            log.debug("表 {} 无初始数据", tableName);
            return;
        }

        log.info("加载表 {} 初始数据，共 {} 条", tableName, dataList.size());

        for (Map<String, Object> data : dataList) {
            if (dataLoader.isIdempotent() && isDataExists(tableName, dataLoader.getUniqueKeys(), data)) {
                log.debug("表 {} 数据已存在，跳过", tableName);
                continue;
            }

            insertData(tableName, data);
        }

        log.info("表 {} 初始数据加载完成", tableName);
    }

    /**
     * 检查数据是否存在
     */
    private boolean isDataExists(String tableName, List<String> uniqueKeys, Map<String, Object> data) {
        if (uniqueKeys.isEmpty()) {
            return false;
        }

        String whereClause = uniqueKeys.stream()
            .map(key -> key + " = ?")
            .collect(Collectors.joining(" AND "));

        String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE " + whereClause;
        Object[] values = uniqueKeys.stream()
            .map(data::get)
            .toArray();

        try {
            Integer count = jdbcTemplate.queryForObject(sql, values, Integer.class);
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("检查数据是否存在失败: {}", tableName, e);
            return false;
        }
    }

    /**
     * 插入数据
     */
    private void insertData(String tableName, Map<String, Object> data) {
        String columns = data.keySet().stream()
            .collect(Collectors.joining(", "));

        String placeholders = data.keySet().stream()
            .map(k -> "?")
            .collect(Collectors.joining(", "));

        String sql = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + placeholders + ")";
        Object[] values = data.values().toArray();

        jdbcTemplate.update(sql, values);
        log.debug("插入数据到表 {}: {}", tableName, data);
    }
}