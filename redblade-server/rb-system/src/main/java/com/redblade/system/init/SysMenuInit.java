package com.redblade.system.init;

import com.redblade.init.metadata.annotation.DbMetaData;
import com.redblade.init.metadata.domain.BaseDml;
import com.redblade.init.metadata.domain.MenuDto;
import com.redblade.system.model.UserModel;

import java.util.List;

/**
 * 系统菜单初始化数据
 *
 * 使用示例：
 * <pre>
 * // 方式1：使用 addMenu() 自动扁平化子菜单
 * addMenu(MenuDto.directory("system", 1, "系统管理", "system")
 *     .icon("setting")
 *     .addChild(MenuDto.menu("system.user", 1, "用户管理", "user", "system/user/index")
 *         .icon("user")
 *         .addAuthority(UserModel.class)));
 *
 * // 方式2：单独添加每个菜单
 * add(MenuDto.directory("system", 1, "系统管理", "system").icon("setting"));
 * add(MenuDto.menu("system.user", 1, "用户管理", "user", "system/user/index").parent("system"));
 * </pre>
 */
@DbMetaData(
    table = "sys_menu",
    order = 10,
    idempotent = true,
    uniqueKeys = {"menu_code"},
    description = "系统管理菜单"
)
public class SysMenuInit extends BaseDml<MenuDto> {

    public SysMenuInit() {
        // 系统管理目录（包含子菜单）
        addMenu(MenuDto.directory("system", 1, "系统管理", "system")
            .icon("setting")
            .addChildren(List.of(
                // 用户管理菜单
                MenuDto.menu("system.user", 1, "用户管理", "user", "system/user/index")
                    .icon("user")
                    .addAuthority(UserModel.class),

                // 角色管理菜单
                MenuDto.menu("system.role", 2, "角色管理", "role", "system/role/index")
                    .icon("peoples"),

                // 菜单管理菜单
                MenuDto.menu("system.menu", 3, "菜单管理", "menu", "system/menu/index")
                    .icon("tree-table"),

                // 部门管理菜单
                MenuDto.menu("system.dept", 4, "部门管理", "dept", "system/dept/index")
                    .icon("tree"),

                // 组织管理菜单
                MenuDto.menu("system.org", 5, "组织管理", "org", "system/org/index")
                    .icon("tree")
            )));

        // 用户管理按钮权限
        add(MenuDto.button("system.user.add", 1, "新增", "system:user:add").parent("system.user"));
        add(MenuDto.button("system.user.edit", 2, "修改", "system:user:edit").parent("system.user"));
        add(MenuDto.button("system.user.delete", 3, "删除", "system:user:delete").parent("system.user"));
        add(MenuDto.button("system.user.export", 4, "导出", "system:user:export").parent("system.user"));

        // 角色管理按钮权限
        add(MenuDto.button("system.role.add", 1, "新增", "system:role:add").parent("system.role"));
        add(MenuDto.button("system.role.edit", 2, "修改", "system:role:edit").parent("system.role"));
        add(MenuDto.button("system.role.delete", 3, "删除", "system:role:delete").parent("system.role"));
    }
}
