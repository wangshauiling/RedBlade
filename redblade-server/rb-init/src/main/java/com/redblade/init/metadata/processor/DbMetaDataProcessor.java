package com.redblade.init.metadata.processor;

import com.redblade.common.enums.DbType;
import com.redblade.init.config.InitProperties;
import com.redblade.init.core.DataLoader;
import com.redblade.init.core.TableCreator;
import com.redblade.init.core.VersionManager;
import com.redblade.init.metadata.domain.BaseDml;
import com.redblade.init.metadata.domain.TableDefinitionBuilder;
import com.redblade.init.metadata.scanner.DbMetaDataScanner;
import com.redblade.init.metadata.scanner.DbMetaDataScanner.DataMetaData;
import com.redblade.init.metadata.scanner.DbMetaDataScanner.TableMetaData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 注解驱动的数据库初始化处理器
 * 扫描 @DbTable 和 @DbMetaData 注解并执行初始化
 *
 * 在正常启动模式下自动执行
 * 在 init-db 模式下由 DbInitLauncher 调用
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DbMetaDataProcessor implements ApplicationRunner {

    private final DbMetaDataScanner scanner;
    private final InitProperties initProperties;
    private final VersionManager versionManager;
    private final TableCreator tableCreator;
    private final DataLoader dataLoader;
    private final JdbcTemplate jdbcTemplate;

    /**
     * 要扫描的包路径
     */
    private final String[] basePackages = {"com.redblade"};

    /**
     * 是否已执行
     */
    private boolean executed = false;

    @Override
    public void run(ApplicationArguments args) {
        // 在 init-db profile 下，由 DbInitLauncher 调用，这里跳过
        if (args != null) {
            for (String arg : args.getSourceArgs()) {
                if (arg.equals("-db") || arg.equals("--db-init")) {
                    return;
                }
            }
        }

        execute();
    }

    /**
     * 执行初始化（可被外部调用）
     */
    public synchronized void execute() {
        if (executed) {
            return;
        }
        executed = true;

        if (!initProperties.isEnabled()) {
            log.debug("数据库初始化已禁用");
            return;
        }

        log.info("============================================================");
        log.info("           RedBlade 数据库初始化（注解驱动模式）");
        log.info("============================================================");
        log.info("");

        long startTime = System.currentTimeMillis();

        try {
            // 1. 扫描元数据
            scanner.scan(basePackages);

            // 2. 初始化版本表
            initVersionTable();

            // 3. 创建表结构
            createTables();

            // 4. 加载初始数据
            loadInitData();

            long cost = System.currentTimeMillis() - startTime;
            log.info("");
            log.info("============================================================");
            log.info("         数据库初始化完成，耗时: {}ms", cost);
            log.info("============================================================");

        } catch (Exception e) {
            log.error("数据库初始化失败", e);
            throw new RuntimeException("数据库初始化失败", e);
        }
    }

    /**
     * 初始化版本表
     */
    private void initVersionTable() {
        log.info(">>> 初始化版本控制表...");
        DbType dbType = getDbType();
        versionManager.initVersionTable(dbType);
        log.info("    版本控制表就绪");
    }

    /**
     * 创建表结构
     */
    private void createTables() {
        log.info(">>> 开始创建表结构...");

        List<TableMetaData> tables = scanner.getTableMetaDatas();
        if (tables.isEmpty()) {
            log.info("    无表定义需要创建");
            return;
        }

        DbType dbType = getDbType();
        int created = 0;
        int skipped = 0;

        // 按版本排序
        tables = tables.stream()
            .sorted(Comparator.comparingInt(TableMetaData::getVersion))
            .toList();

        for (TableMetaData metaData : tables) {
            String tableName = metaData.getTableName();
            try {
                boolean needCreate = versionManager.needUpdate(tableName, metaData.getVersion());
                if (needCreate) {
                    log.info("    [创建] {} - {}", tableName, metaData.getComment());
                    createTable(metaData, dbType);
                    created++;
                } else {
                    log.debug("    [跳过] {} - 已是最新版本", tableName);
                    skipped++;
                }
            } catch (Exception e) {
                log.error("    [失败] {} - {}", tableName, e.getMessage());
            }
        }

        log.info("    表结构创建完成: 创建={}, 跳过={}", created, skipped);
    }

    /**
     * 创建单个表
     */
    private void createTable(TableMetaData metaData, DbType dbType) {
        TableDefinitionBuilder builder = metaData.getBuilder();
        if (builder == null) {
            log.warn("    表 {} 没有定义构建器，跳过", metaData.getTableName());
            return;
        }

        // 转换为 TableDefinition
        com.redblade.init.metadata.TableDefinition tableDefinition = convertToTableDefinition(metaData, builder);

        // 创建表
        tableCreator.createTable(tableDefinition, dbType);
    }

    /**
     * 转换为 TableDefinition
     */
    private com.redblade.init.metadata.TableDefinition convertToTableDefinition(TableMetaData metaData,
                                                                                  TableDefinitionBuilder builder) {
        return new com.redblade.init.metadata.TableDefinition() {
            @Override
            public String getTableName() {
                return metaData.getTableName();
            }

            @Override
            public String getComment() {
                return metaData.getComment();
            }

            @Override
            public List<com.redblade.init.metadata.ColumnDefinition> getColumns() {
                return builder.getColumns().stream()
                    .map(col -> com.redblade.init.metadata.ColumnDefinition.builder()
                        .name(col.getName())
                        .type(col.getType())
                        .length(col.getLength())
                        .scale(col.getScale())
                        .primaryKey(col.isPrimaryKey())
                        .autoIncrement(col.isAutoIncrement())
                        .nullable(col.isNullable())
                        .defaultValue(col.getDefaultValue())
                        .comment(col.getComment())
                        .build())
                    .toList();
            }

            @Override
            public List<com.redblade.init.metadata.IndexDefinition> getIndexes() {
                return builder.getIndexes().stream()
                    .map(idx -> com.redblade.init.metadata.IndexDefinition.builder()
                        .name(idx.getName())
                        .columns(idx.getColumns())
                        .unique(idx.isUnique())
                        .build())
                    .toList();
            }

            @Override
            public int getVersion() {
                return metaData.getVersion();
            }
        };
    }

    /**
     * 加载初始数据
     */
    private void loadInitData() {
        log.info(">>> 开始加载初始数据...");

        List<DataMetaData> dataList = scanner.getDataMetaDatas();
        if (dataList.isEmpty()) {
            log.info("    无初始数据需要加载");
            return;
        }

        for (DataMetaData metaData : dataList) {
            String tableName = metaData.getTable();
            BaseDml<?> dml = metaData.getDml();

            if (dml == null || dml.isEmpty()) {
                continue;
            }

            log.info("    加载: {} (order={}, count={})", tableName, metaData.getOrder(), dml.size());

            try {
                List<Map<String, Object>> data = dml.toMapList();
                loadDataWithIdempotent(tableName, data, metaData);
            } catch (Exception e) {
                log.error("    加载失败: {} - {}", tableName, e.getMessage());
            }
        }

        log.info("    初始数据加载完成");
    }

    /**
     * 加载数据（支持幂等）
     */
    private void loadDataWithIdempotent(String tableName, List<Map<String, Object>> dataList,
                                         DataMetaData metaData) {
        for (Map<String, Object> data : dataList) {
            if (metaData.isIdempotent() && isDataExists(tableName, metaData.getUniqueKeys(), data)) {
                log.debug("    数据已存在，跳过: {}", data);
                continue;
            }
            insertData(tableName, data);
        }
    }

    /**
     * 检查数据是否存在
     */
    private boolean isDataExists(String tableName, List<String> uniqueKeys, Map<String, Object> data) {
        if (uniqueKeys == null || uniqueKeys.isEmpty()) {
            return false;
        }

        String whereClause = String.join(" AND ",
            uniqueKeys.stream().map(key -> key + " = ?").toList());

        String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE " + whereClause;
        Object[] values = uniqueKeys.stream().map(data::get).toArray();

        try {
            Integer count = jdbcTemplate.queryForObject(sql, values, Integer.class);
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 插入数据
     */
    private void insertData(String tableName, Map<String, Object> data) {
        String columns = String.join(", ", data.keySet());
        String placeholders = String.join(", ", data.keySet().stream().map(k -> "?").toList());

        String sql = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + placeholders + ")";
        jdbcTemplate.update(sql, data.values().toArray());
    }

    /**
     * 获取数据库类型
     */
    private DbType getDbType() {
        return DbType.fromCode(initProperties.getDatabaseType());
    }
}
