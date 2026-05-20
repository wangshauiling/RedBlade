package com.redblade.init.core;

import com.redblade.common.enums.DbType;
import com.redblade.database.dialect.Dialect;
import com.redblade.database.dialect.DialectFactory;
import com.redblade.init.metadata.ColumnDefinition;
import com.redblade.init.metadata.DataType;
import com.redblade.init.metadata.IndexDefinition;
import com.redblade.init.metadata.TableDefinition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 表结构创建器
 * 支持表创建和增量变更（新增字段、修改字段类型/长度）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TableCreator {

    private final JdbcTemplate jdbcTemplate;
    private final DialectFactory dialectFactory;
    private final VersionManager versionManager;

    /**
     * 创建或更新表
     */
    public void createTable(TableDefinition tableDefinition, DbType dbType) {
        String tableName = tableDefinition.getTableName();

        if (isTableExists(tableName, dbType)) {
            // 表已存在，检查是否需要变更
            int currentVersion = versionManager.getCurrentVersion(tableName);
            if (currentVersion < tableDefinition.getVersion()) {
                log.info("表 {} 版本从 {} 变更为 {}，执行增量变更", tableName, currentVersion, tableDefinition.getVersion());
                alterTable(tableDefinition, dbType);
                versionManager.updateVersion(tableName, tableDefinition.getVersion());
            } else {
                log.info("表 {} 已存在且版本未变化，跳过", tableName);
            }
            return;
        }

        // 表不存在，创建新表
        createNewTable(tableDefinition, dbType);
    }

    /**
     * 创建新表
     */
    private void createNewTable(TableDefinition tableDefinition, DbType dbType) {
        String tableName = tableDefinition.getTableName();

        String sql = generateCreateTableSql(tableDefinition, dbType);
        log.info("创建表: {}", tableName);
        log.debug("SQL: {}", sql);

        jdbcTemplate.execute(sql);

        // 创建索引
        createIndexes(tableDefinition, dbType);

        // 添加表注释
        addTableComment(tableDefinition, dbType);

        // 添加字段注释
        addColumnComments(tableDefinition, dbType);

        // 更新版本记录
        versionManager.updateVersion(tableName, tableDefinition.getVersion());

        log.info("表 {} 创建完成", tableName);
    }

    /**
     * 变更表结构（新增字段、修改字段）
     */
    private void alterTable(TableDefinition tableDefinition, DbType dbType) {
        String tableName = tableDefinition.getTableName();

        // 获取现有字段信息
        Map<String, ColumnInfo> existingColumns = getExistingColumns(tableName, dbType);

        // 获取现有索引
        Set<String> existingIndexes = getExistingIndexes(tableName, dbType);

        // 处理字段变更
        for (ColumnDefinition newColumn : tableDefinition.getColumns()) {
            String columnName = newColumn.getName();

            if (!existingColumns.containsKey(columnName)) {
                // 新增字段
                addColumn(tableName, newColumn, dbType);
            } else {
                // 检查是否需要修改字段
                ColumnInfo existing = existingColumns.get(columnName);
                if (needModifyColumn(existing, newColumn, dbType)) {
                    modifyColumn(tableName, newColumn, dbType);
                }
            }
        }

        // 处理索引变更
        for (IndexDefinition index : tableDefinition.getIndexes()) {
            if (!existingIndexes.contains(index.getName().toLowerCase())) {
                createIndex(tableName, index);
            }
        }

        // 更新注释
        addTableComment(tableDefinition, dbType);
        addColumnComments(tableDefinition, dbType);
    }

    /**
     * 获取现有字段信息
     */
    private Map<String, ColumnInfo> getExistingColumns(String tableName, DbType dbType) {
        String sql = switch (dbType) {
            case POSTGRESQL -> """
                SELECT column_name, data_type, character_maximum_length, numeric_precision, numeric_scale, is_nullable
                FROM information_schema.columns
                WHERE table_name = ?
                """;
            case ORACLE -> """
                SELECT column_name, data_type, data_length, data_precision, data_scale, nullable
                FROM user_tab_columns
                WHERE table_name = UPPER(?)
                """;
        };

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            ColumnInfo info = new ColumnInfo();
            info.name = rs.getString(1).toLowerCase();
            info.dataType = rs.getString(2);
            info.length = rs.getObject(3) != null ? rs.getInt(3) : null;
            info.precision = rs.getObject(4) != null ? rs.getInt(4) : null;
            info.scale = rs.getObject(5) != null ? rs.getInt(5) : null;
            info.nullable = "YES".equalsIgnoreCase(rs.getString(6));
            return info;
        }, tableName.toLowerCase()).stream().collect(Collectors.toMap(c -> c.name, c -> c));
    }

    /**
     * 获取现有索引
     */
    private Set<String> getExistingIndexes(String tableName, DbType dbType) {
        String sql = switch (dbType) {
            case POSTGRESQL -> """
                SELECT indexname
                FROM pg_indexes
                WHERE tablename = ?
                """;
            case ORACLE -> """
                SELECT index_name
                FROM user_indexes
                WHERE table_name = UPPER(?)
                """;
        };

        return jdbcTemplate.queryForList(sql, String.class, tableName.toLowerCase())
            .stream()
            .map(String::toLowerCase)
            .collect(Collectors.toSet());
    }

    /**
     * 判断是否需要修改字段
     */
    private boolean needModifyColumn(ColumnInfo existing, ColumnDefinition newColumn, DbType dbType) {
        // 检查类型变化
        String newType = generateDataType(newColumn.getType(), newColumn.getLength(), newColumn.getScale(), dbType);
        String existingType = normalizeDataType(existing, dbType);

        if (!newType.equalsIgnoreCase(existingType)) {
            return true;
        }

        // 检查非空约束变化（只处理从可空变为非空的情况）
        if (!newColumn.isNullable() && existing.nullable) {
            return true;
        }

        return false;
    }

    /**
     * 标准化数据类型用于比较
     */
    private String normalizeDataType(ColumnInfo info, DbType dbType) {
        String type = info.dataType.toUpperCase();

        // PostgreSQL 类型标准化
        if (dbType == DbType.POSTGRESQL) {
            if (type.equals("CHARACTER VARYING")) type = "VARCHAR";
            if (type.equals("CHARACTER")) type = "CHAR";
            if (type.equals("INTEGER")) type = "INT";
            if (type.equals("BIGINT") && info.precision != null) type = "BIGINT";
            if (type.equals("TIMESTAMP WITHOUT TIME ZONE")) type = "TIMESTAMP";

            if (info.length != null && (type.equals("VARCHAR") || type.equals("CHAR"))) {
                return type + "(" + info.length + ")";
            }
            if (info.precision != null && type.equals("NUMERIC")) {
                if (info.scale != null) {
                    return "DECIMAL(" + info.precision + ", " + info.scale + ")";
                }
                return "DECIMAL(" + info.precision + ")";
            }
        }

        // Oracle 类型标准化
        if (dbType == DbType.ORACLE) {
            if (type.equals("VARCHAR2")) {
                return "VARCHAR2(" + info.length + ")";
            }
            if (type.equals("NUMBER")) {
                if (info.precision != null) {
                    if (info.scale != null) {
                        return "NUMBER(" + info.precision + ", " + info.scale + ")";
                    }
                    return "NUMBER(" + info.precision + ")";
                }
            }
        }

        return type;
    }

    /**
     * 新增字段
     */
    private void addColumn(String tableName, ColumnDefinition column, DbType dbType) {
        String sql = String.format("ALTER TABLE %s ADD COLUMN %s",
            tableName, generateColumnSql(column, dbType));
        log.info("新增字段: {}.{}", tableName, column.getName());
        log.debug("SQL: {}", sql);
        jdbcTemplate.execute(sql);

        // 添加字段注释
        if (column.getComment() != null) {
            String commentSql = String.format("COMMENT ON COLUMN %s.%s IS '%s'",
                tableName, column.getName(), column.getComment());
            jdbcTemplate.execute(commentSql);
        }
    }

    /**
     * 修改字段
     */
    private void modifyColumn(String tableName, ColumnDefinition column, DbType dbType) {
        String columnName = column.getName();

        // PostgreSQL 使用 ALTER COLUMN
        if (dbType == DbType.POSTGRESQL) {
            // 修改类型
            String typeSql = String.format("ALTER TABLE %s ALTER COLUMN %s TYPE %s",
                tableName, columnName,
                generateDataType(column.getType(), column.getLength(), column.getScale(), dbType));
            log.info("修改字段类型: {}.{}", tableName, columnName);
            log.debug("SQL: {}", typeSql);
            jdbcTemplate.execute(typeSql);

            // 修改非空约束
            if (!column.isNullable()) {
                String nullSql = String.format("ALTER TABLE %s ALTER COLUMN %s SET NOT NULL",
                    tableName, columnName);
                jdbcTemplate.execute(nullSql);
            }
        }

        // Oracle 使用 MODIFY
        if (dbType == DbType.ORACLE) {
            String sql = String.format("ALTER TABLE %s MODIFY %s",
                tableName, generateColumnSql(column, dbType));
            log.info("修改字段: {}.{}", tableName, columnName);
            log.debug("SQL: {}", sql);
            jdbcTemplate.execute(sql);
        }

        // 更新字段注释
        if (column.getComment() != null) {
            String commentSql = String.format("COMMENT ON COLUMN %s.%s IS '%s'",
                tableName, column.getName(), column.getComment());
            jdbcTemplate.execute(commentSql);
        }
    }

    /**
     * 创建单个索引
     */
    private void createIndex(String tableName, IndexDefinition index) {
        String indexSql = generateIndexSql(tableName, index);
        log.info("创建索引: {}", index.getName());
        log.debug("SQL: {}", indexSql);
        jdbcTemplate.execute(indexSql);
    }

    /**
     * 检查表是否存在
     */
    private boolean isTableExists(String tableName, DbType dbType) {
        try {
            String sql = switch (dbType) {
                case ORACLE -> "SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME = UPPER(?)";
                case POSTGRESQL -> "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = ?";
            };
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, tableName.toLowerCase());
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("检查表是否存在失败: {}", tableName, e);
            return false;
        }
    }

    /**
     * 生成建表SQL
     */
    private String generateCreateTableSql(TableDefinition tableDefinition, DbType dbType) {
        StringBuilder sql = new StringBuilder();
        String tableName = tableDefinition.getTableName();

        sql.append("CREATE TABLE ").append(tableName).append(" (\n");

        // 字段定义
        List<ColumnDefinition> columns = tableDefinition.getColumns();
        String columnSql = columns.stream()
            .map(col -> "    " + generateColumnSql(col, dbType))
            .collect(Collectors.joining(",\n"));
        sql.append(columnSql);

        // 主键
        List<ColumnDefinition> pkColumns = columns.stream()
            .filter(ColumnDefinition::isPrimaryKey)
            .toList();
        if (!pkColumns.isEmpty()) {
            String pkName = "pk_" + tableName;
            String pkColumnsStr = pkColumns.stream()
                .map(ColumnDefinition::getName)
                .collect(Collectors.joining(", "));
            sql.append(",\n    CONSTRAINT ").append(pkName).append(" PRIMARY KEY (").append(pkColumnsStr).append(")");
        }

        sql.append("\n)");

        return sql.toString();
    }

    /**
     * 生成字段SQL
     */
    private String generateColumnSql(ColumnDefinition column, DbType dbType) {
        StringBuilder sql = new StringBuilder();
        sql.append(column.getName()).append(" ");

        // 数据类型
        sql.append(generateDataType(column.getType(), column.getLength(), column.getScale(), dbType));

        // 自增
        if (column.isAutoIncrement()) {
            if (dbType == DbType.POSTGRESQL) {
                sql = new StringBuilder(column.getName()).append(" SERIAL");
            } else if (dbType == DbType.ORACLE) {
                // Oracle 使用 SEQUENCE，此处不处理，由单独逻辑创建
            }
        }

        // 非空
        if (!column.isNullable()) {
            sql.append(" NOT NULL");
        }

        // 默认值
        if (column.getDefaultValue() != null) {
            sql.append(" DEFAULT ").append(column.getDefaultValue());
        }

        return sql.toString();
    }

    /**
     * 生成数据类型
     */
    private String generateDataType(DataType type, Integer length, Integer scale, DbType dbType) {
        return switch (type) {
            case VARCHAR -> {
                int len = length != null ? length : 255;
                yield dbType == DbType.ORACLE ? "VARCHAR2(" + len + ")" : "VARCHAR(" + len + ")";
            }
            case CHAR -> {
                int len = length != null ? length : 1;
                yield "CHAR(" + len + ")";
            }
            case TEXT -> dbType == DbType.ORACLE ? "CLOB" : "TEXT";
            case INT -> dbType == DbType.ORACLE ? "NUMBER(10)" : "INTEGER";
            case BIGINT -> dbType == DbType.ORACLE ? "NUMBER(19)" : "BIGINT";
            case SMALLINT -> dbType == DbType.ORACLE ? "NUMBER(5)" : "SMALLINT";
            case FLOAT -> dbType == DbType.ORACLE ? "BINARY_FLOAT" : "FLOAT";
            case DOUBLE -> dbType == DbType.ORACLE ? "BINARY_DOUBLE" : "DOUBLE PRECISION";
            case DECIMAL -> {
                int len = length != null ? length : 10;
                int s = scale != null ? scale : 2;
                yield "DECIMAL(" + len + ", " + s + ")";
            }
            case DATETIME -> dbType == DbType.ORACLE ? "DATE" : "TIMESTAMP";
            case DATE -> "DATE";
            case TIME -> dbType == DbType.ORACLE ? "DATE" : "TIME";
            case BOOLEAN -> dbType == DbType.ORACLE ? "NUMBER(1)" : "BOOLEAN";
            case BLOB -> "BLOB";
            case CLOB -> "CLOB";
        };
    }

    /**
     * 创建索引
     */
    private void createIndexes(TableDefinition tableDefinition, DbType dbType) {
        List<IndexDefinition> indexes = tableDefinition.getIndexes();
        if (indexes == null || indexes.isEmpty()) {
            return;
        }

        for (IndexDefinition index : indexes) {
            createIndex(tableDefinition.getTableName(), index);
        }
    }

    /**
     * 生成索引SQL
     */
    private String generateIndexSql(String tableName, IndexDefinition index) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE ");
        if (index.isUnique()) {
            sql.append("UNIQUE ");
        }
        sql.append("INDEX ").append(index.getName());
        sql.append(" ON ").append(tableName);
        sql.append(" (").append(String.join(", ", index.getColumns())).append(")");
        return sql.toString();
    }

    /**
     * 添加表注释
     */
    private void addTableComment(TableDefinition tableDefinition, DbType dbType) {
        if (tableDefinition.getComment() == null) {
            return;
        }
        String sql = String.format("COMMENT ON TABLE %s IS '%s'",
            tableDefinition.getTableName(), tableDefinition.getComment());
        jdbcTemplate.execute(sql);
    }

    /**
     * 添加字段注释
     */
    private void addColumnComments(TableDefinition tableDefinition, DbType dbType) {
        for (ColumnDefinition column : tableDefinition.getColumns()) {
            if (column.getComment() != null) {
                String sql = String.format("COMMENT ON COLUMN %s.%s IS '%s'",
                    tableDefinition.getTableName(), column.getName(), column.getComment());
                jdbcTemplate.execute(sql);
            }
        }
    }

    /**
     * 字段信息
     */
    private static class ColumnInfo {
        String name;
        String dataType;
        Integer length;
        Integer precision;
        Integer scale;
        boolean nullable;
    }
}
