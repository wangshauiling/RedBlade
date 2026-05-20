package com.redblade.database.interceptor;

import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.redblade.auth.security.LoginUser;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.*;

/**
 * 组织数据隔离拦截器
 * 自动为 SQL 添加 org_code 条件
 */
@Slf4j
@Component
@Intercepts({
    @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})
})
public class OrgDataInterceptor implements Interceptor {

    private static final String ORG_CODE_FIELD = "org_code";

    /**
     * 不需要组织隔离的表（系统级表）
     */
    private static final Set<String> EXCLUDE_TABLES = Set.of(
        "sys_org",
        "sys_init_version"
    );

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) PluginUtils.realTarget(invocation.getTarget());
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);

        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("mappedStatement");
        SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();

        // 获取当前用户组织编码
        String orgCode = getCurrentOrgCode();
        if (orgCode == null) {
            return invocation.proceed();
        }

        BoundSql boundSql = statementHandler.getBoundSql();
        String sql = boundSql.getSql();

        try {
            if (sqlCommandType == SqlCommandType.SELECT) {
                sql = processSelectSql(sql, orgCode);
            } else if (sqlCommandType == SqlCommandType.INSERT) {
                sql = processInsertSql(sql, orgCode);
            } else if (sqlCommandType == SqlCommandType.UPDATE) {
                sql = processUpdateSql(sql, orgCode);
            } else if (sqlCommandType == SqlCommandType.DELETE) {
                sql = processDeleteSql(sql, orgCode);
            }

            // 更新 SQL
            if (!sql.equals(boundSql.getSql())) {
                Field field = boundSql.getClass().getDeclaredField("sql");
                field.setAccessible(true);
                field.set(boundSql, sql);
            }
        } catch (Exception e) {
            log.warn("组织数据隔离处理失败: {}", e.getMessage());
        }

        return invocation.proceed();
    }

    /**
     * 获取当前用户组织编码
     */
    private String getCurrentOrgCode() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
                return loginUser.getOrgCode();
            }
        } catch (Exception e) {
            log.debug("获取当前组织编码失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 处理 SELECT 语句
     */
    private String processSelectSql(String sql, String orgCode) {
        // 简化处理：使用正则添加 WHERE 条件
        // 实际项目中建议使用 JSqlParser 完整解析
        if (sql.toLowerCase().contains(" where ")) {
            sql = sql.replaceAll("(?i) where ", " WHERE " + ORG_CODE_FIELD + " = '" + orgCode + "' AND ");
        } else {
            sql = sql.replaceAll("(?i) from (\\w+)", " FROM $1 WHERE " + ORG_CODE_FIELD + " = '" + orgCode + "'");
        }
        return sql;
    }

    /**
     * 处理 INSERT 语句
     */
    private String processInsertSql(String sql, String orgCode) {
        // INSERT 语句通过 MyBatis-Plus 自动填充处理
        return sql;
    }

    /**
     * 处理 UPDATE 语句
     */
    private String processUpdateSql(String sql, String orgCode) {
        if (sql.toLowerCase().contains(" where ")) {
            sql = sql.replaceAll("(?i) where ", " WHERE " + ORG_CODE_FIELD + " = '" + orgCode + "' AND ");
        } else {
            sql = sql + " WHERE " + ORG_CODE_FIELD + " = '" + orgCode + "'";
        }
        return sql;
    }

    /**
     * 处理 DELETE 语句
     */
    private String processDeleteSql(String sql, String orgCode) {
        if (sql.toLowerCase().contains(" where ")) {
            sql = sql.replaceAll("(?i) where ", " WHERE " + ORG_CODE_FIELD + " = '" + orgCode + "' AND ");
        } else {
            sql = sql + " WHERE " + ORG_CODE_FIELD + " = '" + orgCode + "'";
        }
        return sql;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }
}