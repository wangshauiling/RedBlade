package com.redblade.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体（含组织字段）
 */
@Data
@TableName("sys_user")
public class SysUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 组织编码（联合主键之一）
     */
    @TableField(value = "org_code", fill = FieldFill.INSERT)
    private String orgCode;

    /**
     * 用户编码（联合主键之一）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String userCode;

    /**
     * 部门编码
     */
    private String deptCode;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 性别（0男 1女 2未知）
     */
    private String gender;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 状态（0正常 1停用）
     */
    private String status;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

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