package com.redblade.database.interceptor;

import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.redblade.auth.security.LoginUser;
import com.redblade.database.annotation.DataScope;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.*;
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
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.*;

/**
 * 数据权限拦截器
 * 支持组织隔离和数据权限范围过滤
 */
@Slf4j
@Component
@Intercepts({
    @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})
})
public class DataScopeInterceptor implements Interceptor {

    private static final String ORG_CODE_FIELD = "org_code";

    /**
     * 不需要组织隔离的表（系统级表）
     */
    private static final Set<String> EXCLUDE_TABLES = Set.of(
        "sys_org",
        "sys_init_version"
    );

    /**
     * 数据权限范围类型
     */
    private static final String DATA_SCOPE_ALL = "1";           // 全部数据
    private static final String DATA_SCOPE_CUSTOM = "2";        // 自定义数据
    private static final String DATA_SCOPE_DEPT = "3";          // 本组织数据
    private static final String DATA_SCOPE_DEPT_AND_CHILD = "4"; // 本组织及以下数据
    private static final String DATA_SCOPE_SELF = "5";          // 仅本人数据

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) PluginUtils.realTarget(invocation.getTarget());
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);

        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("mappedStatement");
        SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();

        // 获取当前用户信息
        LoginUser loginUser = getLoginUser();
        if (loginUser == null) {
            return invocation.proceed();
        }

        // 超级管理员不做数据权限过滤
        if (loginUser.getRoles() != null && loginUser.getRoles().contains("admin")) {
            return invocation.proceed();
        }

        String orgCode = loginUser.getOrgCode();
        String dataScope = loginUser.getDataScope();

        BoundSql boundSql = statementHandler.getBoundSql();
        String sql = boundSql.getSql();

        try {
            // 解析并处理 SQL
            Statement statement = CCJSqlParserUtil.parse(sql);
            String processedSql = processStatement(statement, sqlCommandType, orgCode, dataScope, loginUser);

            if (processedSql != null && !processedSql.equals(sql)) {
                Field field = boundSql.getClass().getDeclaredField("sql");
                field.setAccessible(true);
                field.set(boundSql, processedSql);
            }
        } catch (JSQLParserException e) {
            log.warn("SQL解析失败，使用原始SQL: {}", e.getMessage());
        } catch (Exception e) {
            log.warn("数据权限处理失败: {}", e.getMessage());
        }

        return invocation.proceed();
    }

    /**
     * 处理 SQL 语句
     */
    private String processStatement(Statement statement, SqlCommandType sqlCommandType,
                                    String orgCode, String dataScope, LoginUser loginUser) {
        if (statement instanceof Select select) {
            return processSelect(select, orgCode, dataScope, loginUser);
        } else if (statement instanceof Update update) {
            return processUpdate(update, orgCode, dataScope, loginUser);
        } else if (statement instanceof Delete delete) {
            return processDelete(delete, orgCode, dataScope, loginUser);
        }
        return statement.toString();
    }

    /**
     * 处理 SELECT 语句
     */
    private String processSelect(Select select, String orgCode, String dataScope, LoginUser loginUser) {
        SelectBody selectBody = select.getSelectBody();
        if (selectBody instanceof PlainSelect plainSelect) {
            processPlainSelect(plainSelect, orgCode, dataScope, loginUser);
        } else if (selectBody instanceof SetOperationList setOperationList) {
            for (SelectBody body : setOperationList.getSelects()) {
                if (body instanceof PlainSelect plainSelect) {
                    processPlainSelect(plainSelect, orgCode, dataScope, loginUser);
                }
            }
        }
        return select.toString();
    }

    /**
     * 处理 PlainSelect
     */
    private void processPlainSelect(PlainSelect plainSelect, String orgCode, String dataScope, LoginUser loginUser) {
        FromItem fromItem = plainSelect.getFromItem();
        if (fromItem instanceof Table table) {
            if (!isExcludeTable(table.getName())) {
                Expression where = plainSelect.getWhere();
                Expression dataScopeExpr = buildDataScopeExpression(table, orgCode, dataScope, loginUser);

                if (dataScopeExpr != null) {
                    if (where != null) {
                        plainSelect.setWhere(new AndExpression(where, dataScopeExpr));
                    } else {
                        plainSelect.setWhere(dataScopeExpr);
                    }
                }
            }
        }

        // 处理 JOIN
        if (plainSelect.getJoins() != null) {
            for (Join join : plainSelect.getJoins()) {
                if (join.getRightItem() instanceof Table table && !isExcludeTable(table.getName())) {
                    Expression onExpression = join.getOnExpression();
                    Expression dataScopeExpr = buildDataScopeExpression(table, orgCode, dataScope, loginUser);

                    if (dataScopeExpr != null) {
                        if (onExpression != null) {
                            join.setOnExpression(new AndExpression(onExpression, dataScopeExpr));
                        } else {
                            join.setOnExpression(dataScopeExpr);
                        }
                    }
                }
            }
        }
    }

    /**
     * 处理 UPDATE 语句
     */
    private String processUpdate(Update update, String orgCode, String dataScope, LoginUser loginUser) {
        Table table = update.getTable();
        if (!isExcludeTable(table.getName())) {
            Expression where = update.getWhere();
            Expression dataScopeExpr = buildDataScopeExpression(table, orgCode, dataScope, loginUser);

            if (dataScopeExpr != null) {
                if (where != null) {
                    update.setWhere(new AndExpression(where, dataScopeExpr));
                } else {
                    update.setWhere(dataScopeExpr);
                }
            }
        }
        return update.toString();
    }

    /**
     * 处理 DELETE 语句
     */
    private String processDelete(Delete delete, String orgCode, String dataScope, LoginUser loginUser) {
        Table table = delete.getTable();
        if (!isExcludeTable(table.getName())) {
            Expression where = delete.getWhere();
            Expression dataScopeExpr = buildDataScopeExpression(table, orgCode, dataScope, loginUser);

            if (dataScopeExpr != null) {
                if (where != null) {
                    delete.setWhere(new AndExpression(where, dataScopeExpr));
                } else {
                    delete.setWhere(dataScopeExpr);
                }
            }
        }
        return delete.toString();
    }

    /**
     * 构建数据权限表达式
     */
    private Expression buildDataScopeExpression(Table table, String orgCode, String dataScope, LoginUser loginUser) {
        Column orgCodeColumn = new Column(ORG_CODE_FIELD);

        switch (dataScope) {
            case DATA_SCOPE_ALL:
                // 全部数据权限，不添加过滤条件
                return null;

            case DATA_SCOPE_DEPT:
                // 本组织数据
                EqualsTo equalsTo = new EqualsTo();
                equalsTo.setLeftExpression(orgCodeColumn);
                equalsTo.setRightExpression(new StringValue(orgCode));
                return equalsTo;

            case DATA_SCOPE_DEPT_AND_CHILD:
                // 本组织及以下数据
                List<String> orgCodes = loginUser.getOrgCodes();
                if (orgCodes != null && !orgCodes.isEmpty()) {
                    InExpression inExpression = new InExpression();
                    inExpression.setLeftExpression(orgCodeColumn);
                    ExpressionList expressionList = new ExpressionList();
                    List<Expression> expressions = new ArrayList<>();
                    for (String code : orgCodes) {
                        expressions.add(new StringValue(code));
                    }
                    expressionList.setExpressions(expressions);
                    inExpression.setRightItemsList(expressionList);
                    return inExpression;
                }
                // 如果没有组织列表，退回到本组织
                EqualsTo fallback = new EqualsTo();
                fallback.setLeftExpression(orgCodeColumn);
                fallback.setRightExpression(new StringValue(orgCode));
                return fallback;

            case DATA_SCOPE_SELF:
                // 仅本人数据
                EqualsTo selfEquals = new EqualsTo();
                selfEquals.setLeftExpression(new Column("create_by"));
                selfEquals.setRightExpression(new StringValue(loginUser.getUserCode()));
                return selfEquals;

            case DATA_SCOPE_CUSTOM:
                // 自定义数据权限
                List<String> customOrgCodes = loginUser.getCustomOrgCodes();
                if (customOrgCodes != null && !customOrgCodes.isEmpty()) {
                    InExpression customIn = new InExpression();
                    customIn.setLeftExpression(orgCodeColumn);
                    ExpressionList customList = new ExpressionList();
                    List<Expression> customExpressions = new ArrayList<>();
                    for (String code : customOrgCodes) {
                        customExpressions.add(new StringValue(code));
                    }
                    customList.setExpressions(customExpressions);
                    customIn.setRightItemsList(customList);
                    return customIn;
                }
                return null;

            default:
                // 默认本组织数据
                EqualsTo defaultEquals = new EqualsTo();
                defaultEquals.setLeftExpression(orgCodeColumn);
                defaultEquals.setRightExpression(new StringValue(orgCode));
                return defaultEquals;
        }
    }

    /**
     * 判断是否是排除表
     */
    private boolean isExcludeTable(String tableName) {
        return EXCLUDE_TABLES.contains(tableName.toLowerCase());
    }

    /**
     * 获取当前登录用户
     */
    private LoginUser getLoginUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
                return loginUser;
            }
        } catch (Exception e) {
            log.debug("获取当前登录用户失败: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }
}
