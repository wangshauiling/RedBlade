package com.redblade.system.init.table;

import com.redblade.init.metadata.DataType;
import com.redblade.init.metadata.annotation.DbTable;
import com.redblade.init.metadata.domain.TableDefinitionBuilder;

/**
 * 用户角色关联表定义
 */
@DbTable(
    name = "sys_user_role",
    comment = "用户角色关联表",
    version = 1
)
public class SysUserRoleTableDef extends TableDefinitionBuilder {

    public SysUserRoleTableDef() {
        super("sys_user_role", "用户角色关联表");

        // 组织编码（联合主键）
        orgCode();

        // 用户编码（联合主键）
        column("user_code", DataType.VARCHAR, 50).primaryKey().comment("用户编码");

        // 角色编码（联合主键）
        column("role_code", DataType.VARCHAR, 50).primaryKey().comment("角色编码");

        // 索引
        index("idx_user_code", "user_code");
        index("idx_role_code", "role_code");
        index("idx_org_code", "org_code");
    }
}