package com.redblade.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;

/**
 * 角色菜单关联实体（含组织字段）
 */
@Data
@TableName("sys_role_menu")
public class SysRoleMenu implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 组织编码（联合主键之一）
     */
    @TableField(value = "org_code", fill = FieldFill.INSERT)
    private String orgCode;

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 菜单编码
     */
    private String menuCode;
}