package com.redblade.common.helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 数据库操作助手（主数据源）
 * 提供简洁的 SQL 执行方法，支持命名参数、事务、批量操作
 *
 * 使用示例：
 * <pre>
 * &#64;Autowired
 * private MasterDaoHelper masterDaoHelper;
 *
 * // 1. 检查数据是否存在
 * String lockSql = "SELECT 1 FROM bs_lpn_stock WHERE org_code = :org_code AND ref_lock_code = :pmn_code AND rownum = 1";
 * if (masterDaoHelper.hasData(lockSql, params)) {
 *     throw new BusinessException(messageHelper.getAsFormat("compal.msg.0013"));
 * }
 *
 * // 2. 执行更新
 * String updateSql = "UPDATE bs_lpn_stock SET compal_ind_index = NULL, ref_lock_code = NULL WHERE org_code = :org_code AND ref_lock_code = :pmn_code";
 * int rows = masterDaoHelper.update(updateSql, params);
 *
 * // 3. 查询单条记录
 * String roSql = "SELECT * FROM wms_ro_hd WHERE org_code = :org_code AND compal_qr_code = :compal_qr_code";
 * Map&lt;String, Object&gt; qrRow = masterDaoHelper.selectOne(roSql, params);
 *
 * // 4. 查询多条记录
 * String listSql = "SELECT DISTINCT swh_colour_code value, swh_colour_name label FROM bs_swh_colour WHERE swh_colour_cls IN (0,1) AND is_valid = 'y'";
 * List&lt;Map&lt;String, Object&gt;&gt; dataTable = masterDaoHelper.select(listSql, params);
 * </pre>
 */
@Slf4j
@Component
public class MasterDaoHelper {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final PlatformTransactionManager transactionManager;

    public MasterDaoHelper(JdbcTemplate jdbcTemplate,
                           NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                           PlatformTransactionManager transactionManager) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.transactionManager = transactionManager;
    }

    // ==================== 检查数据是否存在 ====================

    /**
     * 检查数据是否存在
     *
     * @param sql    SQL 语句（使用命名参数 :paramName）
     * @param params 参数 Map
     * @return true 存在数据，false 不存在
     */
    public boolean hasData(String sql, Map<String, Object> params) {
        Integer count = namedParameterJdbcTemplate.queryForObject(
            wrapCountSql(sql), new MapSqlParameterSource(params), Integer.class);
        return count != null && count > 0;
    }

    /**
     * 检查数据是否存在（无参数）
     */
    public boolean hasData(String sql) {
        Integer count = jdbcTemplate.queryForObject(wrapCountSql(sql), Integer.class);
        return count != null && count > 0;
    }

    // ==================== 执行更新 ====================

    /**
     * 执行更新操作（INSERT/UPDATE/DELETE）
     *
     * @param sql    SQL 语句（使用命名参数 :paramName）
     * @param params 参数 Map
     * @return 影响行数
     */
    public int update(String sql, Map<String, Object> params) {
        log.debug("执行更新: {}", sql);
        return namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(params));
    }

    /**
     * 执行更新操作（无参数）
     */
    public int update(String sql) {
        log.debug("执行更新: {}", sql);
        return jdbcTemplate.update(sql);
    }

    /**
     * 批量更新
     *
     * @param sql        SQL 语句
     * @param paramsList 参数列表
     * @return 影响行数数组
     */
    public int[] batchUpdate(String sql, List<Map<String, Object>> paramsList) {
        log.debug("批量更新: {}, 数量: {}", sql, paramsList.size());
        SqlParameterSource[] sources = paramsList.stream()
            .map(MapSqlParameterSource::new)
            .toArray(SqlParameterSource[]::new);
        return namedParameterJdbcTemplate.batchUpdate(sql, sources);
    }

    // ==================== 查询单条记录 ====================

    /**
     * 查询单条记录（返回 Map）
     *
     * @param sql    SQL 语句（使用命名参数 :paramName）
     * @param params 参数 Map
     * @return 单条记录（Map），不存在返回 null
     */
    public Map<String, Object> selectOne(String sql, Map<String, Object> params) {
        List<Map<String, Object>> list = select(sql, params);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 查询单条记录（返回实体）
     *
     * @param sql    SQL 语句
     * @param params 参数 Map
     * @param clazz  实体类
     * @return 实体对象，不存在返回 null
     */
    public <T> T selectOne(String sql, Map<String, Object> params, Class<T> clazz) {
        List<T> list = selectList(sql, params, clazz);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 查询单条记录（无参数）
     */
    public Map<String, Object> selectOne(String sql) {
        List<Map<String, Object>> list = select(sql);
        return list.isEmpty() ? null : list.get(0);
    }

    // ==================== 查询多条记录 ====================

    /**
     * 查询多条记录（返回 Map 列表）
     *
     * @param sql    SQL 语句（使用命名参数 :paramName）
     * @param params 参数 Map
     * @return 记录列表
     */
    public List<Map<String, Object>> select(String sql, Map<String, Object> params) {
        log.debug("执行查询: {}", sql);
        return namedParameterJdbcTemplate.queryForList(sql, new MapSqlParameterSource(params));
    }

    /**
     * 查询多条记录（返回实体列表）
     *
     * @param sql    SQL 语句
     * @param params 参数 Map
     * @param clazz  实体类
     * @return 实体列表
     */
    public <T> List<T> selectList(String sql, Map<String, Object> params, Class<T> clazz) {
        log.debug("执行查询: {}", sql);
        return namedParameterJdbcTemplate.query(
            sql, new MapSqlParameterSource(params), new BeanPropertyRowMapper<>(clazz));
    }

    /**
     * 查询多条记录（无参数）
     */
    public List<Map<String, Object>> select(String sql) {
        log.debug("执行查询: {}", sql);
        return jdbcTemplate.queryForList(sql);
    }

    /**
     * 查询多条记录（无参数，返回实体列表）
     */
    public <T> List<T> selectList(String sql, Class<T> clazz) {
        log.debug("执行查询: {}", sql);
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(clazz));
    }

    // ==================== 查询单个值 ====================

    /**
     * 查询单个值
     *
     * @param sql    SQL 语句
     * @param params 参数 Map
     * @param clazz  返回类型
     * @return 单个值，不存在返回 null
     */
    public <T> T selectValue(String sql, Map<String, Object> params, Class<T> clazz) {
        try {
            return namedParameterJdbcTemplate.queryForObject(
                sql, new MapSqlParameterSource(params), clazz);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 查询计数
     */
    public long count(String sql, Map<String, Object> params) {
        Long count = namedParameterJdbcTemplate.queryForObject(
            sql, new MapSqlParameterSource(params), Long.class);
        return count != null ? count : 0L;
    }

    // ==================== 事务支持 ====================

    /**
     * 在事务中执行操作
     *
     * @param action 要执行的操作
     */
    public void executeInTransaction(Runnable action) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.executeWithoutResult(status -> action.run());
    }

    /**
     * 在事务中执行操作（带返回值）
     *
     * @param action 要执行的操作
     * @return 操作结果
     */
    public <T> T executeInTransaction(Supplier<T> action) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        return template.execute(status -> action.get());
    }

    // ==================== 批量插入 ====================

    /**
     * 批量插入
     *
     * @param tableName 表名
     * @param dataList  数据列表
     * @return 影响行数
     */
    public int batchInsert(String tableName, List<Map<String, Object>> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return 0;
        }

        Map<String, Object> firstRow = dataList.get(0);
        String columns = String.join(", ", firstRow.keySet());
        String placeholders = String.join(", ", firstRow.keySet().stream().map(k -> ":" + k).toList());

        String sql = String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, columns, placeholders);

        log.debug("批量插入: {}, 数量: {}", tableName, dataList.size());

        MapSqlParameterSource[] params = dataList.stream()
            .map(MapSqlParameterSource::new)
            .toArray(MapSqlParameterSource[]::new);

        int[] results = namedParameterJdbcTemplate.batchUpdate(sql, params);
        return results.length;
    }

    // ==================== 辅助方法 ====================

    /**
     * 包装为计数 SQL
     */
    private String wrapCountSql(String sql) {
        if (sql.trim().toLowerCase().startsWith("select count(")) {
            return sql;
        }
        return "SELECT COUNT(*) FROM (" + sql + ") t";
    }
}
