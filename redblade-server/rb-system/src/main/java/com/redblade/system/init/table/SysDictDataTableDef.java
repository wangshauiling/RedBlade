package com.redblade.system.init.table;

import com.redblade.init.metadata.DataType;
import com.redblade.init.metadata.annotation.DbTable;
import com.redblade.init.metadata.domain.TableDefinitionBuilder;

/**
 * 字典数据表定义
 */
@DbTable(
    name = "sys_dict_data",
    comment = "字典数据表",
    version = 1
)
public class SysDictDataTableDef extends TableDefinitionBuilder {

    public SysDictDataTableDef() {
        super("sys_dict_data", "字典数据表");

        // 字典编码（主键）
        column("dict_code", DataType.VARCHAR, 100).primaryKey().comment("字典编码");

        // 字典排序
        column("dict_sort", DataType.INT).defaultValue("0").comment("字典排序");

        // 字典标签
        column("dict_label", DataType.VARCHAR, 100).nullable(false).comment("字典标签");

        // 字典键值
        column("dict_value", DataType.VARCHAR, 100).nullable(false).comment("字典键值");

        // 字典类型
        column("dict_type", DataType.VARCHAR, 100).nullable(false).comment("字典类型");

        // 样式属性
        column("css_class", DataType.VARCHAR, 100).comment("样式属性");

        // 表格回显样式
        column("list_class", DataType.VARCHAR, 100).comment("表格回显样式");

        // 是否默认
        column("is_default", DataType.CHAR, 1).defaultValue("'N'").comment("是否默认（Y是 N否）");

        // 状态
        column("status", DataType.CHAR, 1).defaultValue("'0'").comment("状态（0正常 1停用）");

        // 备注
        column("remark", DataType.VARCHAR, 500).comment("备注");

        // 审计字段
        audit();

        // 索引
        index("idx_dict_type", "dict_type");
    }
}