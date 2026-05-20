package com.redblade.system.init.table;

import com.redblade.init.metadata.DataType;
import com.redblade.init.metadata.annotation.DbTable;
import com.redblade.init.metadata.domain.TableDefinitionBuilder;

/**
 * 部门表定义
 */
@DbTable(
    name = "sys_dept",
    comment = "部门表",
    version = 1,
    orgCode = true,
    logicDelete = true,
    audit = true
)
public class SysDeptTableDef extends TableDefinitionBuilder {

    public SysDeptTableDef() {
        super("sys_dept", "部门表");

        // 组织编码（联合主键）
        orgCode();

        // 部门编码（联合主键）
        column("dept_code", DataType.VARCHAR, 50).primaryKey().comment("部门编码");

        // 父部门编码
        column("parent_code", DataType.VARCHAR, 50).comment("父部门编码");

        // 祖级列表
        column("ancestors", DataType.VARCHAR, 500).comment("祖级列表");

        // 部门名称
        column("dept_name", DataType.VARCHAR, 50).nullable(false).comment("部门名称");

        // 显示顺序
        column("sort", DataType.INT).defaultValue("0").comment("显示顺序");

        // 负责人
        column("leader", DataType.VARCHAR, 20).comment("负责人");

        // 联系电话
        column("phone", DataType.VARCHAR, 20).comment("联系电话");

        // 邮箱
        column("email", DataType.VARCHAR, 50).comment("邮箱");

        // 状态
        column("status", DataType.CHAR, 1).defaultValue("'0'").comment("状态（0正常 1停用）");

        // 备注
        column("remark", DataType.VARCHAR, 255).comment("备注");

        // 审计字段
        audit();

        // 逻辑删除
        logicDelete();

        // 索引
        index("idx_parent_code", "parent_code");
        index("idx_org_code", "org_code");
    }
}