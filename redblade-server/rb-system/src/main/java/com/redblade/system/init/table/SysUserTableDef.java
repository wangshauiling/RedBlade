package com.redblade.system.init.table;

import com.redblade.init.metadata.DataType;
import com.redblade.init.metadata.annotation.DbTable;
import com.redblade.init.metadata.domain.TableDefinitionBuilder;

/**
 * 用户表定义
 */
@DbTable(
    name = "sys_user",
    comment = "用户表",
    version = 1,
    orgCode = true,
    logicDelete = true,
    audit = true
)
public class SysUserTableDef extends TableDefinitionBuilder {

    public SysUserTableDef() {
        super("sys_user", "用户表");

        // 组织编码（联合主键）
        orgCode();

        // 用户编码（联合主键）
        column("user_code", DataType.VARCHAR, 50).primaryKey().comment("用户编码");

        // 部门编码
        column("dept_code", DataType.VARCHAR, 50).comment("部门编码");

        // 用户名
        column("username", DataType.VARCHAR, 50).nullable(false).comment("用户名");

        // 密码
        column("password", DataType.VARCHAR, 100).nullable(false).comment("密码");

        // 昵称
        column("nickname", DataType.VARCHAR, 50).comment("昵称");

        // 邮箱
        column("email", DataType.VARCHAR, 100).comment("邮箱");

        // 手机号
        column("phone", DataType.VARCHAR, 20).comment("手机号");

        // 性别
        column("gender", DataType.CHAR, 1).defaultValue("'2'").comment("性别（0男 1女 2未知）");

        // 头像
        column("avatar", DataType.VARCHAR, 255).comment("头像");

        // 状态
        column("status", DataType.CHAR, 1).defaultValue("'0'").comment("状态（0正常 1停用）");

        // 最后登录时间
        column("last_login_time", DataType.DATETIME).comment("最后登录时间");

        // 审计字段
        audit();

        // 逻辑删除
        logicDelete();

        // 唯一索引
        uniqueIndex("uk_org_username", "org_code", "username");

        // 普通索引
        index("idx_org_code", "org_code");
        index("idx_dept_code", "dept_code");
    }
}
