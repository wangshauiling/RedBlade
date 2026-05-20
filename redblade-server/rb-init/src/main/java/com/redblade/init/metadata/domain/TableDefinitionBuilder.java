package com.redblade.init.metadata.domain;

import com.redblade.init.metadata.DataType;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 表定义基类
 * 提供流式 API 定义表结构
 */
@Data
public class TableDefinitionBuilder {

    /**
     * 表名
     */
    private String tableName;

    /**
     * 表注释
     */
    private String comment;

    /**
     * 版本号
     */
    private int version = 1;

    /**
     * 字段列表
     */
    private List<ColumnBuilder> columns = new ArrayList<>();

    /**
     * 索引列表
     */
    private List<IndexBuilder> indexes = new ArrayList<>();

    /**
     * 当前正在构建的字段
     */
    private ColumnBuilder currentColumn;

    public TableDefinitionBuilder(String tableName, String comment) {
        this.tableName = tableName;
        this.comment = comment;
    }

    /**
     * 创建表定义构建器
     */
    public static TableDefinitionBuilder create(String tableName, String comment) {
        return new TableDefinitionBuilder(tableName, comment);
    }

    /**
     * 添加字段
     */
    public TableDefinitionBuilder column(String name, DataType type) {
        currentColumn = new ColumnBuilder(name, type);
        columns.add(currentColumn);
        return this;
    }

    /**
     * 添加字段（带长度）
     */
    public TableDefinitionBuilder column(String name, DataType type, int length) {
        currentColumn = new ColumnBuilder(name, type, length);
        columns.add(currentColumn);
        return this;
    }

    /**
     * 设置当前字段为主键
     */
    public TableDefinitionBuilder primaryKey() {
        if (currentColumn != null) {
            currentColumn.primaryKey(true);
            currentColumn.nullable(false);
        }
        return this;
    }

    /**
     * 设置当前字段为自增
     */
    public TableDefinitionBuilder autoIncrement() {
        if (currentColumn != null) {
            currentColumn.autoIncrement(true);
        }
        return this;
    }

    /**
     * 设置当前字段是否允许为空
     */
    public TableDefinitionBuilder nullable(boolean nullable) {
        if (currentColumn != null) {
            currentColumn.nullable(nullable);
        }
        return this;
    }

    /**
     * 设置当前字段默认值
     */
    public TableDefinitionBuilder defaultValue(String defaultValue) {
        if (currentColumn != null) {
            currentColumn.defaultValue(defaultValue);
        }
        return this;
    }

    /**
     * 设置当前字段注释
     */
    public TableDefinitionBuilder comment(String comment) {
        if (currentColumn != null) {
            currentColumn.comment(comment);
        }
        return this;
    }

    /**
     * 添加组织编码字段（联合主键）
     */
    public TableDefinitionBuilder orgCode() {
        ColumnBuilder col = new ColumnBuilder("org_code", DataType.VARCHAR, 50);
        col.primaryKey(true).nullable(false).comment("组织编码");
        columns.add(col);
        return this;
    }

    /**
     * 添加逻辑删除字段
     */
    public TableDefinitionBuilder logicDelete() {
        ColumnBuilder col = new ColumnBuilder("del_flag", DataType.CHAR, 1);
        col.defaultValue("'0'").comment("删除标志（0正常 1删除）");
        columns.add(col);
        return this;
    }

    /**
     * 添加审计字段
     */
    public TableDefinitionBuilder audit() {
        columns.add(new ColumnBuilder("create_by", DataType.BIGINT).comment("创建人"));
        columns.add(new ColumnBuilder("create_time", DataType.DATETIME).comment("创建时间"));
        columns.add(new ColumnBuilder("update_by", DataType.BIGINT).comment("更新人"));
        columns.add(new ColumnBuilder("update_time", DataType.DATETIME).comment("更新时间"));
        return this;
    }

    /**
     * 添加索引
     */
    public TableDefinitionBuilder index(String name, String... columns) {
        indexes.add(new IndexBuilder(name, List.of(columns), false));
        return this;
    }

    /**
     * 添加唯一索引
     */
    public TableDefinitionBuilder uniqueIndex(String name, String... columns) {
        indexes.add(new IndexBuilder(name, List.of(columns), true));
        return this;
    }

    /**
     * 设置版本号
     */
    public TableDefinitionBuilder version(int version) {
        this.version = version;
        return this;
    }

    /**
     * 字段构建器
     */
    @Data
    public static class ColumnBuilder {
        private String name;
        private DataType type;
        private Integer length;
        private Integer scale;
        private boolean primaryKey;
        private boolean autoIncrement;
        private boolean nullable = true;
        private String defaultValue;
        private String comment;

        public ColumnBuilder(String name, DataType type) {
            this.name = name;
            this.type = type;
        }

        public ColumnBuilder(String name, DataType type, int length) {
            this.name = name;
            this.type = type;
            this.length = length;
        }

        public ColumnBuilder primaryKey(boolean primaryKey) {
            this.primaryKey = primaryKey;
            return this;
        }

        public ColumnBuilder autoIncrement(boolean autoIncrement) {
            this.autoIncrement = autoIncrement;
            return this;
        }

        public ColumnBuilder nullable(boolean nullable) {
            this.nullable = nullable;
            return this;
        }

        public ColumnBuilder defaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public ColumnBuilder comment(String comment) {
            this.comment = comment;
            return this;
        }
    }

    /**
     * 索引构建器
     */
    @Data
    public static class IndexBuilder {
        private String name;
        private List<String> columns;
        private boolean unique;

        public IndexBuilder(String name, List<String> columns, boolean unique) {
            this.name = name;
            this.columns = columns;
            this.unique = unique;
        }
    }
}
