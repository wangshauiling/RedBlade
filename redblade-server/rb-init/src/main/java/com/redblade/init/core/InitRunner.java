package com.redblade.init.core;

import com.redblade.common.enums.DbType;
import com.redblade.init.config.InitProperties;
import com.redblade.init.data.InitDataLoader;
import com.redblade.init.metadata.TableDefinition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * 初始化执行器
 * 应用启动后自动执行数据库初始化
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InitRunner implements ApplicationRunner {

    private final InitProperties initProperties;
    private final TableRegistry tableRegistry;
    private final TableCreator tableCreator;
    private final DataLoader dataLoader;
    private final VersionManager versionManager;

    @Value("${rb.database.type:postgresql}")
    private String databaseType;

    private final List<TableDefinition> tableDefinitions;
    private final List<InitDataLoader> initDataLoaders;

    @Override
    public void run(ApplicationArguments args) {
        if (!initProperties.isEnabled()) {
            log.info("数据库初始化已禁用");
            return;
        }

        if (initProperties.getMode() == InitProperties.InitMode.NONE) {
            log.info("数据库初始化模式为 NONE，跳过初始化");
            return;
        }

        log.info("========== 开始数据库初始化 ==========");

        DbType dbType = DbType.fromCode(databaseType);
        log.info("数据库类型: {}", dbType.getDesc());

        // 初始化版本表
        versionManager.initVersionTable(dbType);

        // 注册表定义
        for (TableDefinition tableDefinition : tableDefinitions) {
            tableRegistry.register(tableDefinition);
        }

        // 创建表
        for (TableDefinition tableDefinition : tableRegistry.getAllSortedByVersion()) {
            try {
                tableCreator.createTable(tableDefinition, dbType);
            } catch (Exception e) {
                log.error("创建表 {} 失败", tableDefinition.getTableName(), e);
            }
        }

        // 加载初始数据
        if (initDataLoaders != null && !initDataLoaders.isEmpty()) {
            List<InitDataLoader> sortedLoaders = initDataLoaders.stream()
                .sorted(Comparator.comparingInt(InitDataLoader::getOrder))
                .toList();

            for (InitDataLoader loader : sortedLoaders) {
                try {
                    dataLoader.loadData(loader);
                } catch (Exception e) {
                    log.error("加载表 {} 初始数据失败", loader.getTableName(), e);
                }
            }
        }

        log.info("========== 数据库初始化完成 ==========");
    }
}