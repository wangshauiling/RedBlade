package com.redblade.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户角色关联实体（含组织字段）
 */
@Data
@TableName("sys_user_role")
public class SysUserRole implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 组织编码（联合主键之一）
     */
    @TableField(value = "org_code", fill = FieldFill.INSERT)
    private String orgCode;

    /**
     * 用户编码
     */
    private String userCode;

    /**
     * 角色编码
     */
    private String roleCode;
}