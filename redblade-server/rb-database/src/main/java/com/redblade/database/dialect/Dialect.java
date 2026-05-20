package com.redblade.database.dialect;

/**
 * 数据库方言接口
 */
public interface Dialect {

    /**
     * 获取当前时间函数
     */
    String currentTimestamp();

    /**
     * 获取分页SQL
     *
     * @param sql    原始SQL
     * @param offset 偏移量
     * @param limit  每页数量
     * @return 分页SQL
     */
    String pagination(String sql, long offset, long limit);

    /**
     * 获取日期格式化函数
     *
     * @param field   字段名
     * @param pattern 格式
     * @return 格式化SQL
     */
    String dateFormat(String field, String pattern);

    /**
     * 获取字符串拼接函数
     *
     * @param strings 字符串列表
     * @return 拼接SQL
     */
    String concat(String... strings);

    /**
     * 获取布尔类型
     */
    String booleanType();

    /**
     * 获取自增主键SQL
     */
    String autoIncrement();
}