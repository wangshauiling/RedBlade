package com.redblade.system.init.table;

import com.redblade.init.metadata.DataType;
import com.redblade.init.metadata.annotation.DbTable;
import com.redblade.init.metadata.domain.TableDefinitionBuilder;

/**
 * 字典类型表定义
 */
@DbTable(
    name = "sys_dict_type",
    comment = "字典类型表",
    version = 1
)
public class SysDictTypeTableDef extends TableDefinitionBuilder {

    public SysDictTypeTableDef() {
        super("sys_dict_type", "字典类型表");

        // 字典编码（主键）
        column("dict_code", DataType.VARCHAR, 100).primaryKey().comment("字典编码");

        // 字典名称
        column("dict_name", DataType.VARCHAR, 100).nullable(false).comment("字典名称");

        // 字典类型
        column("dict_type", DataType.VARCHAR, 100).nullable(false).comment("字典类型");

        // 状态
        column("status", DataType.CHAR, 1).defaultValue("'0'").comment("状态（0正常 1停用）");

        // 备注
        column("remark", DataType.VARCHAR, 500).comment("备注");

        // 审计字段
        audit();

        // 唯一索引
        uniqueIndex("uk_dict_type", "dict_type");
    }
}