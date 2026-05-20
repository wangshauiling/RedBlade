package com.redblade.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色实体（含组织字段）
 */
@Data
@TableName("sys_role")
public class SysRole implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 组织编码（联合主键之一）
     */
    @TableField(value = "org_code", fill = FieldFill.INSERT)
    private String orgCode;

    /**
     * 角色编码（联合主键之一）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String roleCode;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色标识
     */
    private String roleKey;

    /**
     * 数据权限范围（1全部 2自定义 3本部门 4本部门及以下 5仅本人）
     */
    private Integer dataScope;

    /**
     * 显示顺序
     */
    private Integer sort;

    /**
     * 状态（0正常 1停用）
     */
    private String status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 删除标志（0正常 1删除）
     */
    @TableLogic
    private String delFlag;
}