package com.redblade.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据库类型枚举
 */
@Getter
@AllArgsConstructor
public enum DbType {

    ORACLE("oracle", "Oracle数据库"),
    POSTGRESQL("postgresql", "PostgreSQL数据库");

    private final String code;
    private final String desc;

    public static DbType fromCode(String code) {
        for (DbType dbType : values()) {
            if (dbType.getCode().equalsIgnoreCase(code)) {
                return dbType;
            }
        }
        throw new IllegalArgumentException("Unknown database type: " + code);
    }
}