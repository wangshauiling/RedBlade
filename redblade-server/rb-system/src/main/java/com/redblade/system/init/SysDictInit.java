package com.redblade.system.init;

import com.redblade.init.metadata.annotation.DbMetaData;
import com.redblade.init.metadata.domain.BaseDml;
import com.redblade.init.metadata.domain.DictDto;

/**
 * 系统字典初始化数据
 */
@DbMetaData(
    table = "sys_dict_data",
    order = 20,
    idempotent = true,
    uniqueKeys = {"dict_type", "dict_value"},
    description = "系统字典数据"
)
public class SysDictInit extends BaseDml<DictDto> {

    public SysDictInit() {
        // 性别字典
        add(new DictDto("sys_gender", "男", "0", 1));
        add(new DictDto("sys_gender", "女", "1", 2));
        add(new DictDto("sys_gender", "未知", "2", 3));

        // 状态字典
        add(new DictDto("sys_status", "正常", "0", 1));
        add(new DictDto("sys_status", "停用", "1", 2));

        // 菜单类型字典
        add(new DictDto("sys_menu_type", "目录", "M", 1));
        add(new DictDto("sys_menu_type", "菜单", "C", 2));
        add(new DictDto("sys_menu_type", "按钮", "F", 3));

        // 组织类型字典
        add(new DictDto("sys_org_type", "总部", "hq", 1));
        add(new DictDto("sys_org_type", "分公司", "company", 2));
        add(new DictDto("sys_org_type", "部门", "dept", 3));
        add(new DictDto("sys_org_type", "项目组", "team", 4));

        // 数据权限范围字典
        add(new DictDto("sys_data_scope", "全部数据", "1", 1));
        add(new DictDto("sys_data_scope", "本组织数据", "2", 2));
        add(new DictDto("sys_data_scope", "本组织及下级", "3", 3));
        add(new DictDto("sys_data_scope", "仅本人数据", "4", 4));
    }
}
