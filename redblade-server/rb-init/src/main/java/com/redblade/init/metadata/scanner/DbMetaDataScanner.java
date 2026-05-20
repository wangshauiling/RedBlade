package com.redblade.init.metadata.scanner;

import com.redblade.init.metadata.annotation.DbMetaData;
import com.redblade.init.metadata.annotation.DbTable;
import com.redblade.init.metadata.domain.BaseDml;
import com.redblade.init.metadata.domain.TableDefinitionBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 数据库元数据扫描器
 * 扫描所有 @DbTable 和 @DbMetaData 注解的类
 */
@Slf4j
@Component
public class DbMetaDataScanner {

    /**
     * 扫描结果
     */
    private final List<TableMetaData> tableMetaDatas = new ArrayList<>();
    private final List<DataMetaData> dataMetaDatas = new ArrayList<>();

    /**
     * 是否已扫描
     */
    private boolean scanned = false;

    /**
     * 扫描指定包路径下的所有元数据类
     *
     * @param basePackages 基础包路径
     */
    public synchronized void scan(String... basePackages) {
        if (scanned) {
            return;
        }

        log.info(">>> 开始扫描数据库元数据...");

        // 扫描 @DbTable 注解
        scanDbTables(basePackages);

        // 扫描 @DbMetaData 注解
        scanDbMetaData(basePackages);

        scanned = true;

        log.info("    扫描完成: 表定义={}, 数据定义={}", tableMetaDatas.size(), dataMetaDatas.size());
    }

    /**
     * 扫描 @DbTable 注解
     */
    private void scanDbTables(String[] basePackages) {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(DbTable.class));

        for (String basePackage : basePackages) {
            for (BeanDefinition bd : scanner.findCandidateComponents(basePackage)) {
                AnnotatedBeanDefinition definition = (AnnotatedBeanDefinition) bd;
                try {
                    Class<?> clazz = Class.forName(definition.getBeanClassName());
                    DbTable annotation = clazz.getAnnotation(DbTable.class);

                    TableMetaData metaData = new TableMetaData();
                    metaData.setClassName(clazz.getName());
                    metaData.setTableName(annotation.name());
                    metaData.setComment(annotation.comment());
                    metaData.setVersion(annotation.version());
                    metaData.setOrgCode(annotation.orgCode());
                    metaData.setLogicDelete(annotation.logicDelete());
                    metaData.setAudit(annotation.audit());

                    // 尝试实例化并获取表定义
                    if (TableDefinitionBuilder.class.isAssignableFrom(clazz)) {
                        TableDefinitionBuilder builder = (TableDefinitionBuilder) clazz.getDeclaredConstructor().newInstance();
                        metaData.setBuilder(builder);
                    }

                    tableMetaDatas.add(metaData);
                    log.debug("    发现表定义: {} -> {}", annotation.name(), clazz.getSimpleName());

                } catch (Exception e) {
                    log.warn("    加载表定义失败: {} - {}", definition.getBeanClassName(), e.getMessage());
                }
            }
        }
    }

    /**
     * 扫描 @DbMetaData 注解
     */
    private void scanDbMetaData(String[] basePackages) {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(DbMetaData.class));

        for (String basePackage : basePackages) {
            for (BeanDefinition bd : scanner.findCandidateComponents(basePackage)) {
                AnnotatedBeanDefinition definition = (AnnotatedBeanDefinition) bd;
                try {
                    Class<?> clazz = Class.forName(definition.getBeanClassName());
                    DbMetaData annotation = clazz.getAnnotation(DbMetaData.class);

                    DataMetaData metaData = new DataMetaData();
                    metaData.setClassName(clazz.getName());
                    metaData.setTable(annotation.table());
                    metaData.setOrder(annotation.order());
                    metaData.setIdempotent(annotation.idempotent());
                    metaData.setUniqueKeys(Arrays.asList(annotation.uniqueKeys()));
                    metaData.setDescription(annotation.description());

                    // 尝试实例化并获取数据
                    if (BaseDml.class.isAssignableFrom(clazz)) {
                        BaseDml<?> dml = (BaseDml<?>) clazz.getDeclaredConstructor().newInstance();
                        metaData.setDml(dml);
                    }

                    dataMetaDatas.add(metaData);
                    log.debug("    发现数据定义: {} -> {} (order={})", annotation.table(), clazz.getSimpleName(), annotation.order());

                } catch (Exception e) {
                    log.warn("    加载数据定义失败: {} - {}", definition.getBeanClassName(), e.getMessage());
                }
            }
        }
    }

    /**
     * 获取所有表元数据
     */
    public List<TableMetaData> getTableMetaDatas() {
        return tableMetaDatas;
    }

    /**
     * 获取所有数据元数据
     */
    public List<DataMetaData> getDataMetaDatas() {
        // 按顺序排序
        return dataMetaDatas.stream()
            .sorted(Comparator.comparingInt(DataMetaData::getOrder))
            .toList();
    }

    /**
     * 获取指定表的数据元数据
     */
    public List<DataMetaData> getDataMetaDatas(String tableName) {
        return dataMetaDatas.stream()
            .filter(d -> d.getTable().equals(tableName))
            .sorted(Comparator.comparingInt(DataMetaData::getOrder))
            .toList();
    }

    /**
     * 表元数据
     */
    @lombok.Data
    public static class TableMetaData {
        private String className;
        private String tableName;
        private String comment;
        private int version;
        private boolean orgCode;
        private boolean logicDelete;
        private boolean audit;
        private TableDefinitionBuilder builder;
    }

    /**
     * 数据元数据
     */
    @lombok.Data
    public static class DataMetaData {
        private String className;
        private String table;
        private int order;
        private boolean idempotent;
        private List<String> uniqueKeys;
        private String description;
        private BaseDml<?> dml;
    }
}
