package com.redblade.database.dialect;

/**
 * PostgreSQL 数据库方言
 */
public class PostgresqlDialect implements Dialect {

    @Override
    public String currentTimestamp() {
        return "NOW()";
    }

    @Override
    public String pagination(String sql, long offset, long limit) {
        return String.format("%s LIMIT %d OFFSET %d", sql, limit, offset);
    }

    @Override
    public String dateFormat(String field, String pattern) {
        return String.format("TO_CHAR(%s, '%s')", field, pattern);
    }

    @Override
    public String concat(String... strings) {
        return String.join(" || ", strings);
    }

    @Override
    public String booleanType() {
        return "BOOLEAN";
    }

    @Override
    public String autoIncrement() {
        return "SERIAL";
    }
}