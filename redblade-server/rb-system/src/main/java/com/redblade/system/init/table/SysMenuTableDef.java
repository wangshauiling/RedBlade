package com.redblade.system.init.table;

import com.redblade.init.metadata.DataType;
import com.redblade.init.metadata.annotation.DbTable;
import com.redblade.init.metadata.domain.TableDefinitionBuilder;

/**
 * 菜单表定义
 */
@DbTable(
    name = "sys_menu",
    comment = "菜单权限表",
    version = 1
)
public class SysMenuTableDef extends TableDefinitionBuilder {

    public SysMenuTableDef() {
        super("sys_menu", "菜单权限表");

        // 菜单编码（主键）
        column("menu_code", DataType.VARCHAR, 100).primaryKey().comment("菜单编码");

        // 菜单名称
        column("menu_name", DataType.VARCHAR, 50).nullable(false).comment("菜单名称");

        // 父菜单编码
        column("parent_code", DataType.VARCHAR, 100).comment("父菜单编码");

        // 显示顺序
        column("sort", DataType.INT).defaultValue("0").comment("显示顺序");

        // 路由地址
        column("path", DataType.VARCHAR, 200).comment("路由地址");

        // 组件路径
        column("component", DataType.VARCHAR, 255).comment("组件路径");

        // 路由参数
        column("query", DataType.VARCHAR, 255).comment("路由参数");

        // 是否为外链
        column("is_frame", DataType.CHAR, 1).defaultValue("'0'").comment("是否为外链（0否 1是）");

        // 是否缓存
        column("is_cache", DataType.CHAR, 1).defaultValue("'0'").comment("是否缓存（0否 1是）");

        // 菜单类型
        column("menu_type", DataType.CHAR, 1).defaultValue("'C'").comment("菜单类型（M目录 C菜单 F按钮）");

        // 显示状态
        column("visible", DataType.CHAR, 1).defaultValue("'0'").comment("显示状态（0显示 1隐藏）");

        // 菜单状态
        column("status", DataType.CHAR, 1).defaultValue("'0'").comment("菜单状态（0正常 1停用）");

        // 权限标识
        column("permission", DataType.VARCHAR, 100).comment("权限标识");

        // 图标
        column("icon", DataType.VARCHAR, 100).comment("菜单图标");

        // 备注
        column("remark", DataType.VARCHAR, 500).comment("备注");

        // 审计字段
        audit();

        // 普通索引
        index("idx_parent_code", "parent_code");
    }
}