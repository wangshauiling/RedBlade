package com.redblade.system.init.table;

import com.redblade.init.metadata.DataType;
import com.redblade.init.metadata.annotation.DbTable;
import com.redblade.init.metadata.domain.TableDefinitionBuilder;

/**
 * 角色菜单关联表定义
 */
@DbTable(
    name = "sys_role_menu",
    comment = "角色菜单关联表",
    version = 1
)
public class SysRoleMenuTableDef extends TableDefinitionBuilder {

    public SysRoleMenuTableDef() {
        super("sys_role_menu", "角色菜单关联表");

        // 组织编码（联合主键）
        orgCode();

        // 角色编码（联合主键）
        column("role_code", DataType.VARCHAR, 50).primaryKey().comment("角色编码");

        // 菜单编码（联合主键）
        column("menu_code", DataType.VARCHAR, 100).primaryKey().comment("菜单编码");

        // 索引
        index("idx_role_code", "role_code");
        index("idx_menu_code", "menu_code");
        index("idx_org_code", "org_code");
    }
}