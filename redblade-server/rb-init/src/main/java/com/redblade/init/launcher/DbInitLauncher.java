package com.redblade.init.launcher;

import com.redblade.common.enums.DbType;
import com.redblade.init.config.InitProperties;
import com.redblade.init.core.*;
import com.redblade.init.data.InitDataLoader;
import com.redblade.init.metadata.TableDefinition;
import com.redblade.init.metadata.processor.DbMetaDataProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * 数据库初始化启动器
 * 通过 -db 参数触发，仅执行数据库初始化，不启动项目
 */
@Slf4j
@Component
@Profile("init-db")
@RequiredArgsConstructor
public class DbInitLauncher implements ApplicationRunner {

    private final InitProperties initProperties;
    private final TableRegistry tableRegistry;
    private final TableCreator tableCreator;
    private final DataLoader dataLoader;
    private final VersionManager versionManager;
    private final DbMetaDataProcessor dbMetaDataProcessor;

    private final List<TableDefinition> tableDefinitions;
    private final List<InitDataLoader> initDataLoaders;

    @Override
    public void run(ApplicationArguments args) {
        log.info("============================================================");
        log.info("               RedBlade 数据库初始化工具");
        log.info("============================================================");
        log.info("");

        long startTime = System.currentTimeMillis();

        try {
            // 1. 初始化版本表
            initVersionTable();

            // 2. 通过注解扫描注册表定义（优先）
            dbMetaDataProcessor.execute();

            // 3. 注册额外的表定义（如果有）
            registerTableDefinitions();

            // 4. 加载初始数据
            loadInitData();

            long cost = System.currentTimeMillis() - startTime;
            log.info("");
            log.info("============================================================");
            log.info("         数据库初始化完成，耗时: {}ms", cost);
            log.info("============================================================");

        } catch (Exception e) {
            log.error("数据库初始化失败", e);
            System.exit(1);
        }

        // 初始化完成后退出
        System.exit(0);
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
     * 注册表定义
     */
    private void registerTableDefinitions() {
        if (tableDefinitions == null || tableDefinitions.isEmpty()) {
            return;
        }
        log.info(">>> 注册额外表定义...");
        for (TableDefinition tableDefinition : tableDefinitions) {
            tableRegistry.register(tableDefinition);
            log.info("    注册表: {} (v{})", tableDefinition.getTableName(), tableDefinition.getVersion());
        }
        log.info("    共注册 {} 张表", tableRegistry.size());
    }

    /**
     * 加载初始数据
     */
    private void loadInitData() {
        log.info(">>> 开始加载初始数据...");

        if (initDataLoaders == null || initDataLoaders.isEmpty()) {
            log.info("    无初始数据需要加载");
            return;
        }

        List<InitDataLoader> sortedLoaders = initDataLoaders.stream()
            .sorted(Comparator.comparingInt(InitDataLoader::getOrder))
            .toList();

        for (InitDataLoader loader : sortedLoaders) {
            try {
                log.info("    加载: {} (order={})", loader.getTableName(), loader.getOrder());
                dataLoader.loadData(loader);
            } catch (Exception e) {
                log.error("    加载失败: {} - {}", loader.getTableName(), e.getMessage());
            }
        }

        log.info("    初始数据加载完成");
    }

    /**
     * 获取数据库类型
     */
    private DbType getDbType() {
        return DbType.fromCode(initProperties.getDatabaseType());
    }
}