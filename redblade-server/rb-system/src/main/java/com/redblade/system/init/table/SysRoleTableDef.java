package com.redblade.system.init.table;

import com.redblade.init.metadata.DataType;
import com.redblade.init.metadata.annotation.DbTable;
import com.redblade.init.metadata.domain.TableDefinitionBuilder;

/**
 * 角色表定义
 */
@DbTable(
    name = "sys_role",
    comment = "角色表",
    version = 1,
    orgCode = true,
    logicDelete = true,
    audit = true
)
public class SysRoleTableDef extends TableDefinitionBuilder {

    public SysRoleTableDef() {
        super("sys_role", "角色表");

        // 组织编码（联合主键）
        orgCode();

        // 角色编码（联合主键）
        column("role_code", DataType.VARCHAR, 50).primaryKey().comment("角色编码");

        // 角色名称
        column("role_name", DataType.VARCHAR, 50).nullable(false).comment("角色名称");

        // 角色标识
        column("role_key", DataType.VARCHAR, 50).nullable(false).comment("角色标识");

        // 显示顺序
        column("sort", DataType.INT).defaultValue("0").comment("显示顺序");

        // 数据权限范围
        column("data_scope", DataType.CHAR, 1).defaultValue("'1'").comment("数据权限范围（1全部 2本组织 3本组织及下级 4仅本人）");

        // 菜单权限（JSON格式）
        column("menu_check_strictly", DataType.CHAR, 1).defaultValue("'1'").comment("菜单树选择项是否关联显示");

        // 部门权限（JSON格式）
        column("dept_check_strictly", DataType.CHAR, 1).defaultValue("'1'").comment("部门树选择项是否关联显示");

        // 状态
        column("status", DataType.CHAR, 1).defaultValue("'0'").comment("状态（0正常 1停用）");

        // 备注
        column("remark", DataType.VARCHAR, 255).comment("备注");

        // 审计字段
        audit();

        // 逻辑删除
        logicDelete();

        // 唯一索引
        uniqueIndex("uk_org_role_key", "org_code", "role_key");

        // 普通索引
        index("idx_org_code", "org_code");
    }
}
