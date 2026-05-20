package com.redblade.system.init;

import com.redblade.init.metadata.annotation.DbMetaData;
import com.redblade.init.metadata.domain.BaseDml;
import com.redblade.init.metadata.domain.RoleDto;

/**
 * 系统角色初始化数据
 */
@DbMetaData(
    table = "sys_role",
    order = 15,
    idempotent = true,
    uniqueKeys = {"org_code", "role_key"},
    description = "系统默认角色"
)
public class SysRoleInit extends BaseDml<RoleDto> {

    public SysRoleInit() {
        // 超级管理员
        RoleDto admin = new RoleDto("admin", "超级管理员", "admin", 1);
        admin.dataScope("1"); // 全部数据
        admin.remark("超级管理员，拥有所有权限");
        add(admin);

        // 管理员
        RoleDto manager = new RoleDto("manager", "管理员", "manager", 2);
        manager.dataScope("2"); // 本组织数据
        manager.remark("组织管理员，拥有本组织所有权限");
        add(manager);

        // 普通用户
        RoleDto user = new RoleDto("user", "普通用户", "user", 3);
        user.dataScope("4"); // 仅本人数据
        user.remark("普通用户，仅有查看和操作本人数据的权限");
        add(user);
    }
}
