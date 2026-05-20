package com.redblade.system.init.table;

import com.redblade.init.metadata.DataType;
import com.redblade.init.metadata.annotation.DbTable;
import com.redblade.init.metadata.domain.TableDefinitionBuilder;

/**
 * 组织表定义
 */
@DbTable(
    name = "sys_org",
    comment = "组织表",
    version = 1
)
public class SysOrgTableDef extends TableDefinitionBuilder {

    public SysOrgTableDef() {
        super("sys_org", "组织表");

        // 组织编码（主键）
        column("org_code", DataType.VARCHAR, 50).primaryKey().comment("组织编码");

        // 父组织编码
        column("parent_code", DataType.VARCHAR, 50).comment("父组织编码");

        // 组织名称
        column("org_name", DataType.VARCHAR, 100).nullable(false).comment("组织名称");

        // 组织类型
        column("org_type", DataType.VARCHAR, 20).nullable(false).comment("组织类型（hq/company/dept/team）");

        // 组织层级
        column("org_level", DataType.INT).defaultValue("1").comment("组织层级");

        // 组织路径
        column("org_path", DataType.VARCHAR, 200).comment("组织路径");

        // 负责人ID
        column("leader_id", DataType.BIGINT).comment("负责人ID");

        // 联系电话
        column("phone", DataType.VARCHAR, 20).comment("联系电话");

        // 邮箱
        column("email", DataType.VARCHAR, 100).comment("邮箱");

        // 显示顺序
        column("sort", DataType.INT).defaultValue("0").comment("显示顺序");

        // 状态
        column("status", DataType.CHAR, 1).defaultValue("'0'").comment("状态（0正常 1停用）");

        // 备注
        column("remark", DataType.VARCHAR, 255).comment("备注");

        // 审计字段
        audit();

        // 逻辑删除
        logicDelete();

        // 索引
        index("idx_parent_code", "parent_code");
        index("idx_org_path", "org_path");
    }
}
