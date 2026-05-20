package com.redblade.init.core;

import com.redblade.common.enums.DbType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 版本管理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VersionManager {

    private static final String VERSION_TABLE = "sys_init_version";

    private final JdbcTemplate jdbcTemplate;

    /**
     * 初始化版本表
     */
    public void initVersionTable(DbType dbType) {
        if (!isVersionTableExists(dbType)) {
            log.info("创建版本控制表: {}", VERSION_TABLE);
            String sql = switch (dbType) {
                case ORACLE -> String.format("""
                    CREATE TABLE %s (
                        table_name VARCHAR2(100) PRIMARY KEY,
                        version NUMBER(10) NOT NULL,
                        update_time DATE DEFAULT SYSDATE
                    )
                    """, VERSION_TABLE);
                case POSTGRESQL -> String.format("""
                    CREATE TABLE %s (
                        table_name VARCHAR(100) PRIMARY KEY,
                        version INTEGER NOT NULL,
                        update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                    """, VERSION_TABLE);
            };
            jdbcTemplate.execute(sql);
        }
    }

    /**
     * 检查版本表是否存在
     */
    private boolean isVersionTableExists(DbType dbType) {
        try {
            String sql = switch (dbType) {
                case ORACLE -> "SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME = UPPER(?)";
                case POSTGRESQL -> "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = ?";
            };
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, VERSION_TABLE.toLowerCase());
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("检查版本表是否存在失败", e);
            return false;
        }
    }

    /**
     * 获取当前版本
     */
    public int getCurrentVersion(String tableName) {
        try {
            String sql = "SELECT version FROM " + VERSION_TABLE + " WHERE table_name = ?";
            Integer version = jdbcTemplate.queryForObject(sql, Integer.class, tableName.toLowerCase());
            return version != null ? version : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 更新版本
     */
    public void updateVersion(String tableName, int version) {
        String sql;
        if (getCurrentVersion(tableName) > 0) {
            sql = String.format("UPDATE %s SET version = ?, update_time = %s WHERE table_name = ?",
                VERSION_TABLE, getCurrentTimestampFunction());
            jdbcTemplate.update(sql, version, tableName.toLowerCase());
        } else {
            sql = String.format("INSERT INTO %s (table_name, version, update_time) VALUES (?, ?, %s)",
                VERSION_TABLE, getCurrentTimestampFunction());
            jdbcTemplate.update(sql, tableName.toLowerCase(), version);
        }
        log.debug("更新表 {} 版本为 {}", tableName, version);
    }

    /**
     * 检查是否需要更新
     */
    public boolean needUpdate(String tableName, int newVersion) {
        int currentVersion = getCurrentVersion(tableName);
        return currentVersion < newVersion;
    }

    /**
     * 获取当前时间函数
     */
    private String getCurrentTimestampFunction() {
        // 简化处理，实际应根据 dbType 判断
        return "CURRENT_TIMESTAMP";
    }
}