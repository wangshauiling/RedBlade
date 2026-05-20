package com.redblade.system.init;

import com.redblade.init.metadata.annotation.DbMetaData;
import com.redblade.init.metadata.domain.BaseDml;
import com.redblade.init.metadata.domain.MenuDto;

import java.util.List;

/**
 * 业务模块菜单初始化示例
 *
 * 展示二次开发时如何定义业务菜单
 *
 * 使用方式：
 * 1. 创建类继承 BaseDml&lt;MenuDto&gt;
 * 2. 添加 @DbMetaData 注解
 * 3. 在构造函数中使用 addMenu() 或 add() 添加菜单
 */
// @DbMetaData(
//     table = "sys_menu",
//     order = 100,  // 在系统菜单之后加载
//     idempotent = true,
//     uniqueKeys = {"menu_code"},
//     description = "业务模块菜单"
// )
public class BizMenuInitExample extends BaseDml<MenuDto> {

    public BizMenuInitExample() {
        // 示例：WMS 仓储管理模块
        addMenu(MenuDto.directory("wms", 10, "仓储管理", "wms")
            .icon("warehouse")
            .addChildren(List.of(
                // 料箱维护
                MenuDto.menu("wms.box", 1, "料箱维护", "box", "wms/box/index")
                    .icon("box")
                    .addAuthority(BoxModel.class),  // 自动关联 Model

                // 备料维护
                MenuDto.menu("wms.kitlot", 2, "备料维护", "kitlot", "wms/kitlot/index")
                    .icon("list")
                    .addAuthority(KitLotModel.class),

                // 库存查询
                MenuDto.menu("wms.inventory", 3, "库存查询", "inventory", "wms/inventory/index")
                    .icon("search")
            )));

        // 料箱维护按钮权限
        add(MenuDto.button("wms.box.add", 1, "新增", "wms:box:add").parent("wms.box"));
        add(MenuDto.button("wms.box.edit", 2, "修改", "wms:box:edit").parent("wms.box"));
        add(MenuDto.button("wms.box.delete", 3, "删除", "wms:box:delete").parent("wms.box"));
        add(MenuDto.button("wms.box.print", 4, "打印", "wms:box:print").parent("wms.box"));

        // 备料维护按钮权限
        add(MenuDto.button("wms.kitlot.add", 1, "新增", "wms:kitlot:add").parent("wms.kitlot"));
        add(MenuDto.button("wms.kitlot.edit", 2, "修改", "wms:kitlot:edit").parent("wms.kitlot"));
        add(MenuDto.button("wms.kitlot.delete", 3, "删除", "wms:kitlot:delete").parent("wms.kitlot"));
        add(MenuDto.button("wms.kitlot.confirm", 4, "确认", "wms:kitlot:confirm").parent("wms.kitlot"));
    }

    // 示例 Model 类（实际项目中应替换为真实的 Model）
    static class BoxModel {}
    static class KitLotModel {}
}
