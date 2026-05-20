package com.redblade.init.core;

import com.redblade.common.enums.DbType;
import com.redblade.init.api.*;
import com.redblade.init.config.InitProperties;
import com.redblade.init.metadata.TableDefinition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据库初始化引擎
 * 核心初始化逻辑，可被二次开发项目复用
 */
@Slf4j
@RequiredArgsConstructor
public class DbInitEngine {

    private final JdbcTemplate jdbcTemplate;
    private final InitProperties initProperties;
    private final VersionManager versionManager;
    private final TableCreator tableCreator;
    private final DataLoader dataLoader;

    /**
     * 执行数据库初始化
     *
     * @param extensions 扩展点列表（二次开发项目可提供）
     * @param tableDefinitions 表定义列表（二次开发项目可提供）
     * @return 初始化上下文
     */
    public InitContext execute(List<DbInitExtension> extensions,
                               List<TableDefinition> tableDefinitions) {
        InitContext context = new InitContext();
        context.setStartTime(LocalDateTime.now());
        context.setDbType(DbType.fromCode(initProperties.getDatabaseType()));

        try {
            // 1. 初始化版本表
            initVersionTable(context);

            // 2. 收集所有表定义（框架 + 扩展）
            List<TableDefinition> allTables = collectTableDefinitions(tableDefinitions, extensions);

            // 3. 执行扩展点前置回调
            executeBeforeInit(extensions, context);

            // 4. 创建表结构
            createTables(allTables, context);

            // 5. 执行扩展点表创建后回调
            executeAfterTablesCreated(extensions, context);

            // 6. 检查是否首次运行
            checkFirstRun(context);

            // 7. 交互式输入（首次运行）
            if (context.isFirstRun()) {
                interactiveInput(context, extensions);
            }

            // 8. 加载初始数据
            loadInitData(extensions, context);

            // 9. 执行扩展点后置回调
            executeAfterInit(extensions, context);

            log.info("数据库初始化完成");

        } catch (Exception e) {
            log.error("数据库初始化失败", e);
            throw new RuntimeException("数据库初始化失败", e);
        }

        return context;
    }

    /**
     * 初始化版本表
     */
    private void initVersionTable(InitContext context) {
        log.info(">>> 初始化版本控制表...");
        versionManager.initVersionTable(context.getDbType());
        log.info("    版本控制表就绪");
    }

    /**
     * 收集所有表定义
     */
    private List<TableDefinition> collectTableDefinitions(List<TableDefinition> baseTables,
                                                          List<DbInitExtension> extensions) {
        log.info(">>> 收集表定义...");

        List<TableDefinition> allTables = new ArrayList<>(baseTables);

        // 收集扩展点提供的表定义
        for (DbInitExtension extension : extensions) {
            if (extension.isEnabled()) {
                List<TableDefinition> extTables = extension.getTableDefinitions();
                if (extTables != null && !extTables.isEmpty()) {
                    allTables.addAll(extTables);
                    log.info("    扩展 [{}] 提供 {} 张表", extension.getName(), extTables.size());
                }
            }
        }

        log.info("    共收集 {} 张表", allTables.size());
        return allTables;
    }

    /**
     * 创建表结构
     */
    private void createTables(List<TableDefinition> tables, InitContext context) {
        log.info(">>> 创建表结构...");

        int created = 0;
        int skipped = 0;
        int failed = 0;

        // 按版本排序
        List<TableDefinition> sortedTables = tables.stream()
            .sorted(Comparator.comparingInt(TableDefinition::getVersion))
            .toList();

        for (TableDefinition table : sortedTables) {
            String tableName = table.getTableName();
            try {
                boolean needCreate = versionManager.needUpdate(tableName, table.getVersion());
                if (needCreate) {
                    log.info("    [创建] {} - {}", tableName, table.getComment());
                    tableCreator.createTable(table, context.getDbType());
                    created++;
                } else {
                    log.debug("    [跳过] {} - 已是最新版本", tableName);
                    skipped++;
                }
            } catch (Exception e) {
                log.error("    [失败] {} - {}", tableName, e.getMessage());
                failed++;
            }
        }

        context.setTablesCreated(created);
        context.setTablesSkipped(skipped);
        log.info("    表结构创建完成: 创建={}, 跳过={}, 失败={}", created, skipped, failed);
    }

    /**
     * 检查是否首次运行
     */
    private void checkFirstRun(InitContext context) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_org WHERE del_flag = '0'",
                Integer.class
            );
            context.setFirstRun(count == null || count == 0);
        } catch (Exception e) {
            context.setFirstRun(true);
        }
    }

    /**
     * 交互式输入
     */
    private void interactiveInput(InitContext context, List<DbInitExtension> extensions) {
        log.info(">>> 首次运行，进入交互式初始化...");

        UserInput input = new UserInput();

        // 基础信息输入
        input.printTitle("请输入组织信息");
        context.setOrgCode(input.readLine("  组织编码 [默认: ROOT]: ", "ROOT"));
        context.setOrgName(input.readLineWithValidation("  组织名称: ",
            s -> s.isEmpty() ? "组织名称不能为空" : null));

        input.printTitle("请输入管理员账号信息");
        context.setAdminUsername(input.readLine("  用户名 [默认: admin]: ", "admin"));
        context.setAdminPassword(input.readPassword("  密码: "));

        // 扩展点交互式输入
        for (DbInitExtension extension : extensions) {
            if (extension.isEnabled()) {
                extension.onInteractiveInput(context, input);
            }
        }

        // 确认
        input.printTitle("请确认初始化信息");
        System.out.println("  组织编码: " + context.getOrgCode());
        System.out.println("  组织名称: " + context.getOrgName());
        System.out.println("  管理员账号: " + context.getAdminUsername());
        System.out.println();

        if (!input.readConfirm("  确认初始化?")) {
            log.info("用户取消初始化");
            System.exit(0);
        }

        input.close();
    }

    /**
     * 加载初始数据
     */
    private void loadInitData(List<DbInitExtension> extensions, InitContext context) {
        log.info(">>> 加载初始数据...");

        // 收集所有初始数据
        List<InitData> allData = new ArrayList<>();

        // 框架内置初始数据
        allData.addAll(getBuiltinInitData(context));

        // 扩展点提供的初始数据
        for (DbInitExtension extension : extensions) {
            if (extension.isEnabled()) {
                List<InitData> extData = extension.getInitData();
                if (extData != null) {
                    allData.addAll(extData);
                }
            }
        }

        // 按顺序加载
        allData.stream()
            .sorted(Comparator.comparingInt(InitData::getOrder))
            .forEach(data -> {
                log.info("    加载: {} (order={})", data.getTableName(), data.getOrder());
                dataLoader.loadData(convertToDataLoader(data));
            });

        log.info("    初始数据加载完成");
    }

    /**
     * 获取框架内置初始数据
     */
    private List<InitData> getBuiltinInitData(InitContext context) {
        List<InitData> dataList = new ArrayList<>();

        // 组织数据
        Map<String, Object> orgData = new HashMap<>();
        orgData.put("org_code", context.getOrgCode());
        orgData.put("org_name", context.getOrgName());
        orgData.put("org_type", "hq");
        orgData.put("org_level", 1);
        orgData.put("org_path", context.getOrgCode());
        orgData.put("status", "0");
        orgData.put("del_flag", "0");
        dataList.add(InitData.ofSingle("sys_org", orgData).order(1));

        // 管理员数据
        Map<String, Object> adminData = new HashMap<>();
        adminData.put("org_code", context.getOrgCode());
        adminData.put("username", context.getAdminUsername());
        adminData.put("password", Base64.getEncoder().encodeToString(context.getAdminPassword().getBytes()));
        adminData.put("nickname", "管理员");
        adminData.put("status", "0");
        adminData.put("del_flag", "0");
        dataList.add(InitData.ofSingle("sys_user", adminData)
            .order(2).uniqueKeys(List.of("org_code", "username")));

        return dataList;
    }

    /**
     * 转换为 DataLoader 格式
     */
    private com.redblade.init.data.InitDataLoader convertToDataLoader(InitData initData) {
        return new com.redblade.init.data.InitDataLoader() {
            @Override
            public String getTableName() {
                return initData.getTableName();
            }

            @Override
            public int getOrder() {
                return initData.getOrder();
            }

            @Override
            public List<Map<String, Object>> getData() {
                return initData.getData();
            }

            @Override
            public boolean isIdempotent() {
                return initData.isIdempotent();
            }

            @Override
            public List<String> getUniqueKeys() {
                return initData.getUniqueKeys() != null ? initData.getUniqueKeys() : List.of();
            }
        };
    }

    /**
     * 执行扩展点前置回调
     */
    private void executeBeforeInit(List<DbInitExtension> extensions, InitContext context) {
        for (DbInitExtension extension : getSortedExtensions(extensions)) {
            if (extension.isEnabled()) {
                try {
                    extension.beforeInit(context);
                } catch (Exception e) {
                    log.warn("扩展 [{}] beforeInit 执行失败: {}", extension.getName(), e.getMessage());
                }
            }
        }
    }

    /**
     * 执行扩展点表创建后回调
     */
    private void executeAfterTablesCreated(List<DbInitExtension> extensions, InitContext context) {
        for (DbInitExtension extension : getSortedExtensions(extensions)) {
            if (extension.isEnabled()) {
                try {
                    extension.afterTablesCreated(context);
                } catch (Exception e) {
                    log.warn("扩展 [{}] afterTablesCreated 执行失败: {}", extension.getName(), e.getMessage());
                }
            }
        }
    }

    /**
     * 执行扩展点后置回调
     */
    private void executeAfterInit(List<DbInitExtension> extensions, InitContext context) {
        for (DbInitExtension extension : getSortedExtensions(extensions)) {
            if (extension.isEnabled()) {
                try {
                    extension.afterInit(context);
                } catch (Exception e) {
                    log.warn("扩展 [{}] afterInit 执行失败: {}", extension.getName(), e.getMessage());
                }
            }
        }
    }

    /**
     * 获取排序后的扩展点
     */
    private List<DbInitExtension> getSortedExtensions(List<DbInitExtension> extensions) {
        return extensions.stream()
            .sorted(Comparator.comparingInt(DbInitExtension::getOrder))
            .toList();
    }
}