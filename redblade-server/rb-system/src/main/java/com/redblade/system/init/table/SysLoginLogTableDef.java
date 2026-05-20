package com.redblade.system.init.table;

import com.redblade.init.metadata.DataType;
import com.redblade.init.metadata.annotation.DbTable;
import com.redblade.init.metadata.domain.TableDefinitionBuilder;

/**
 * 登录日志表定义
 */
@DbTable(
    name = "sys_login_log",
    comment = "登录日志表",
    version = 1
)
public class SysLoginLogTableDef extends TableDefinitionBuilder {

    public SysLoginLogTableDef() {
        super("sys_login_log", "登录日志表");

        // 日志编码（主键）
        column("log_code", DataType.VARCHAR, 50).primaryKey().comment("日志编码");

        // 用户账号
        column("user_name", DataType.VARCHAR, 50).comment("用户账号");

        // 用户编码
        column("user_code", DataType.VARCHAR, 50).comment("用户编码");

        // 组织编码
        column("org_code", DataType.VARCHAR, 50).comment("组织编码");

        // IP地址
        column("ipaddr", DataType.VARCHAR, 128).comment("IP地址");

        // 登录地点
        column("login_location", DataType.VARCHAR, 255).comment("登录地点");

        // 浏览器
        column("browser", DataType.VARCHAR, 50).comment("浏览器");

        // 操作系统
        column("os", DataType.VARCHAR, 50).comment("操作系统");

        // 登录状态
        column("status", DataType.CHAR, 1).defaultValue("'0'").comment("登录状态（0成功 1失败）");

        // 提示消息
        column("msg", DataType.VARCHAR, 255).comment("提示消息");

        // 登录时间
        column("login_time", DataType.DATETIME).comment("登录时间");

        // 索引
        index("idx_login_time", "login_time");
        index("idx_user_name", "user_name");
        index("idx_org_code", "org_code");
        index("idx_user_code", "user_code");
    }
}