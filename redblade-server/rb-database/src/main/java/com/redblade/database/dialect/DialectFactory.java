package com.redblade.database.dialect;

import com.redblade.common.enums.DbType;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据库方言工厂
 */
@Component
public class DialectFactory {

    private final Map<DbType, Dialect> dialectMap = new ConcurrentHashMap<>();

    public DialectFactory() {
        dialectMap.put(DbType.ORACLE, new OracleDialect());
        dialectMap.put(DbType.POSTGRESQL, new PostgresqlDialect());
    }

    /**
     * 获取数据库方言
     */
    public Dialect getDialect(DbType dbType) {
        Dialect dialect = dialectMap.get(dbType);
        if (dialect == null) {
            throw new IllegalArgumentException("Unsupported database type: " + dbType);
        }
        return dialect;
    }

    /**
     * 注册方言
     */
    public void register(DbType dbType, Dialect dialect) {
        dialectMap.put(dbType, dialect);
    }
}