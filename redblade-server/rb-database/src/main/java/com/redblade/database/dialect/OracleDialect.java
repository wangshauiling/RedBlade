package com.redblade.database.dialect;

/**
 * Oracle 数据库方言
 */
public class OracleDialect implements Dialect {

    @Override
    public String currentTimestamp() {
        return "SYSDATE";
    }

    @Override
    public String pagination(String sql, long offset, long limit) {
        long endRow = offset + limit;
        return String.format(
            "SELECT * FROM (SELECT ROWNUM AS rowno, t.* FROM (%s) t WHERE ROWNUM <= %d) WHERE rowno > %d",
            sql, endRow, offset
        );
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
        return "NUMBER(1)";
    }

    @Override
    public String autoIncrement() {
        // Oracle 使用 SEQUENCE，此处返回空，由建表逻辑处理
        return "";
    }
}