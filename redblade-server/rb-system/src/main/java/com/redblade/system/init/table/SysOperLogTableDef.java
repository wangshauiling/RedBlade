package com.redblade.system.init.table;

import com.redblade.init.metadata.DataType;
import com.redblade.init.metadata.annotation.DbTable;
import com.redblade.init.metadata.domain.TableDefinitionBuilder;

/**
 * 操作日志表定义
 */
@DbTable(
    name = "sys_oper_log",
    comment = "操作日志表",
    version = 1
)
public class SysOperLogTableDef extends TableDefinitionBuilder {

    public SysOperLogTableDef() {
        super("sys_oper_log", "操作日志表");

        // 日志编码（主键）
        column("log_code", DataType.VARCHAR, 50).primaryKey().comment("日志编码");

        // 模块标题
        column("title", DataType.VARCHAR, 50).comment("模块标题");

        // 业务类型
        column("business_type", DataType.INT).defaultValue("0").comment("业务类型（0其它 1新增 2修改 3删除）");

        // 方法名称
        column("method", DataType.VARCHAR, 200).comment("方法名称");

        // 请求方式
        column("request_method", DataType.VARCHAR, 10).comment("请求方式");

        // 操作人员
        column("oper_name", DataType.VARCHAR, 50).comment("操作人员");

        // 操作人员编码
        column("oper_code", DataType.VARCHAR, 50).comment("操作人员编码");

        // 组织编码
        column("org_code", DataType.VARCHAR, 50).comment("组织编码");

        // 操作网址
        column("oper_url", DataType.VARCHAR, 255).comment("操作网址");

        // 操作IP地址
        column("oper_ip", DataType.VARCHAR, 128).comment("操作IP地址");

        // 操作地点
        column("oper_location", DataType.VARCHAR, 255).comment("操作地点");

        // 请求参数
        column("oper_param", DataType.TEXT).comment("请求参数");

        // 返回参数
        column("json_result", DataType.TEXT).comment("返回参数");

        // 操作状态
        column("status", DataType.INT).defaultValue("0").comment("操作状态（0正常 1异常）");

        // 错误消息
        column("error_msg", DataType.TEXT).comment("错误消息");

        // 操作时间
        column("oper_time", DataType.DATETIME).comment("操作时间");

        // 消耗时间
        column("cost_time", DataType.BIGINT).defaultValue("0").comment("消耗时间");

        // 索引
        index("idx_oper_time", "oper_time");
        index("idx_org_code", "org_code");
        index("idx_oper_code", "oper_code");
    }
}